package com.dujia.java_gobang.game;

import com.dujia.java_gobang.common.utils.WebSocketUtils;
import com.dujia.java_gobang.model.dataobject.User;
import com.dujia.java_gobang.model.response.MatchResponse;
import com.dujia.java_gobang.model.result.WebSocketResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.*;

// 更新
//   - 线程
//     使用@PostConstruct和@PreDestroy注解创建和销毁线程
//   - 锁
//     不在使用锁，而是使用线程安全的BlockingDeque提高效率
//   - 数据结构
//     使用userMap来充当用户登记表，避免每次调用remove方法都无条件的遍历队列

/**
 * 管理匹配时的逻辑
 * 匹配和取消匹配的真正实现
 */
@Slf4j
@Component
public class Matcher {
    // 三个队列对应三个级别的玩家
    private final BlockingDeque<User> normalQueue = new LinkedBlockingDeque<>();
    private final BlockingDeque<User> highQueue = new LinkedBlockingDeque<>();
    private final BlockingDeque<User> strongestQueue = new LinkedBlockingDeque<>();
    private final ConcurrentHashMap<Integer, User> userMap = new ConcurrentHashMap<>();

    private Thread normalMatchThread;
    private Thread highMatchThread;
    private Thread strongestMatchThread;

    @Autowired
    private OnlineUserManger onlineUserManger;
    @Autowired
    private RoomManger roomManger;


    /**
     * 在Bean初始化后启动线程
     */
    @PostConstruct
    public void startThread() {
        log.info("[Matcher::startThread] 创建线程");
        normalMatchThread = createThread("Normal-Matcher", normalQueue);
        highMatchThread = createThread("High-Matcher", highQueue);
        strongestMatchThread = createThread("Strongest-Matcher", strongestQueue);

        normalMatchThread.start();
        highMatchThread.start();
        strongestMatchThread.start();
    }

    /**
     * Bean销毁之前结束线程
     */
    @PreDestroy
    public void stopThread() {
        log.info("[Matcher::stopThread] 销毁线程");
        if (normalMatchThread != null) {
            normalMatchThread.interrupt();
        }
        if (highMatchThread != null) {
            highMatchThread.interrupt();
        }
        if (strongestMatchThread != null) {
            strongestMatchThread.interrupt();
        }
    }

    private Thread createThread(String threadName, BlockingDeque<User> blockingDeque) {
        Thread thread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                handlerMatch(blockingDeque);
            }
            log.warn("[Matcher::createThread] {}线程停止", threadName);
        }, threadName);
        thread.setDaemon(true);
        return thread;
    }

    public void add(User user) {
        if (userMap.putIfAbsent(user.getUserId(), user) != null) {
            log.warn("[Matcher::add] 玩家{}已在匹配队列，请勿重复添加", user.getUserId());
            return;
        }
        BlockingDeque<User> queue = getUserQueue(user.getScore());
        // 将user加入匹配信息表

        try {
            queue.put(user); // 当队列满了时，使用put方法会阻塞队列
            log.info("[Matcher::add] 用户{}已加入匹配队列{}", user.getUserId(), getQueueName(user.getScore()));
        } catch (InterruptedException e) {
            // 出现问题，回滚
            userMap.remove(user.getUserId());
            Thread.currentThread().interrupt();
        }
    }

    private String getQueueName(int score) {
        if (score < 2000) {
            return "普通";
        }
        if (score < 3000) {
            return "高手";
        }
        return "大师";
    }

    public void remove(User user) {
        // 如果用户在userMap里才删除，不在就不删除
        // 先在userMap删除
        if (userMap.remove(user.getUserId()) != null) {
            // 再去queue删除
            BlockingDeque<User> queue = getUserQueue(user.getScore());
            queue.remove(user);
        }
    }

    private BlockingDeque<User> getUserQueue(int score) {
        if (score < 2000) {
            return normalQueue;
        } else if (score < 3000) {
            return highQueue;
        } else {
            return strongestQueue;
        }
    }


    /**
     * 处理单个队列的匹配
     *
     * @param matchQueue 当前匹配的队列
     */
    private void handlerMatch(BlockingDeque<User> matchQueue) {
        try {
            // 关键点：在取出player1后，立刻从userMap中移除，防止在等待player2期间被remove方法错误操作
            User player1 = matchQueue.take();
            userMap.remove(player1.getUserId());
            User player2 = matchQueue.take();
            userMap.remove(player2.getUserId());

            if (player1.getUserId().equals(player2.getUserId())) {
                log.warn("[Matcher::handlerMatch] 出现自己匹配自己的情况！User: {}. 本次匹配已作废。", player1.getUserId());
                return;
            }

            log.info("[Matcher::handlerMatch] 玩家{} vs 玩家{}", player1.getUserId(), player2.getUserId());

            WebSocketSession session1 = onlineUserManger.getFromGameHall(player1.getUserId());
            WebSocketSession session2 = onlineUserManger.getFromGameHall(player2.getUserId());

            if (session1 == null && session2 == null) {
                log.warn("[Matcher::handlerMatch] 玩家 {} 和 {} 都已离线", player1.getUserId(), player2.getUserId());
                // 两个人都掉线了，什么都不用做
                return;
            }
            if (session1 == null) {
                log.warn("[Matcher::handlerMatch] 玩家{}离线", player1.getUserId());
                matchQueue.offerFirst(player2);
                userMap.put(player2.getUserId(), player2);
                return;
            }
            if (session2 == null) {
                log.warn("[Matcher::handlerMatch] 玩家{}离线", player2.getUserId());
                matchQueue.offerFirst(player1);
                userMap.put(player1.getUserId(), player1);
                return;
            }

            // 创建房间
            Room room = new Room();
            roomManger.add(room, player1.getUserId(), player2.getUserId());

            // 通知两个玩家
            MatchResponse response = new MatchResponse();
            response.setOk(true);
            response.setMessage("matchSuccess");
            WebSocketUtils.sendMessage(session1, WebSocketResult.success(response));
            WebSocketUtils.sendMessage(session2, WebSocketResult.success(response));
        } catch (InterruptedException e) {
            // take出现异常，关闭线程
            Thread.currentThread().interrupt();
            log.error("[Matcher::handlerMatch] 线程{}异常，已关闭", matchQueue.getClass().getCanonicalName());
        } catch (Exception e) {
            // 其他异常，打印堆栈信息
            log.error("[Matcher::handlerMatch] 匹配线程出现未知异常", e);
        }
    }
}