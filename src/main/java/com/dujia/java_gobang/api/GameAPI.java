package com.dujia.java_gobang.api;

import com.dujia.java_gobang.common.utils.WebSocketUtils;
import com.dujia.java_gobang.constant.Constants;
import com.dujia.java_gobang.game.*;
import com.dujia.java_gobang.model.dataobject.User;
import com.dujia.java_gobang.model.result.GameConnectionResult;
import com.dujia.java_gobang.model.result.WebSocketResult;
import com.dujia.java_gobang.service.GameService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;
import java.io.IOException;

@Slf4j
@Component
public class GameAPI extends TextWebSocketHandler {

    @Autowired
    private OnlineUserManger onlineUserManger;
    @Autowired
    private RoomManger roomManger;
    @Resource(name = "gameService")
    private GameService gameService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        User user = (User) session.getAttributes().get(Constants.SESSION_KEY);

        GameConnectionResult result = gameService.handleConnectionEstablished(user);

        if (result.getSingUserPayload() != null) {
            log.warn("[GameAPI::afterConnectionEstablished] 校验失败");
            WebSocketUtils.sendMessage(session, WebSocketResult.fail(result.getSingUserPayload()));
            return;
        }

        // 更新会话
        onlineUserManger.enterGameRoom(user.getUserId(), session);

        if (result.isNeedsToNotifyAll()) {
            log.warn("[GameAPI::afterConnectionEstablished] 通知两个用户");
            Room room = roomManger.getRoomByUserId(user.getUserId());
            if (room != null && room.getUser1() != null && room.getUser2() != null) {
                WebSocketSession session1 = onlineUserManger.getFromGameRoom(room.getUser1().getUserId());
                WebSocketSession session2 = onlineUserManger.getFromGameRoom(room.getUser2().getUserId());

                result.getNotificationPayload().setThisUserId(room.getUser1().getUserId());
                result.getNotificationPayload().setThatUserId(room.getUser2().getUserId());
                WebSocketUtils.sendMessage(session1, WebSocketResult.success(result.getNotificationPayload()));

                result.getNotificationPayload().setThisUserId(room.getUser2().getUserId());
                result.getNotificationPayload().setThatUserId(room.getUser1().getUserId());
                WebSocketUtils.sendMessage(session2, WebSocketResult.success(result.getNotificationPayload()));
            }
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        User user = (User) session.getAttributes().get(Constants.SESSION_KEY);
        if (user == null) {
            log.warn("[GameAPI::handleTextMessage] 玩家未登录");
            return;
        }
        log.info("[GameAPI::handleTextMessage] 玩家{}摆放棋子", user.getUserId());

        Room room = roomManger.getRoomByUserId(user.getUserId());
        GameResponse response = gameService.handlePutChess(room, message.getPayload());
        if (response == null) {
            return;
        }

        // 3. 判断有没有人掉线
        WebSocketSession session1 = onlineUserManger.getFromGameRoom(room.getUser1().getUserId());
        WebSocketSession session2 = onlineUserManger.getFromGameRoom(room.getUser2().getUserId());
        if (session1 == null) {
            log.info("[GameAPI::handleTextMessage] 玩家{}掉线", room.getUser1().getUserId());
            response.setWinner(room.getUser2().getUserId());
        }
        if (session2 == null) {
            log.info("[GameAPI::handleTextMessage] 玩家{}掉线", room.getUser2().getUserId());
            response.setWinner(room.getUser1().getUserId());
        }
        if (session1 == null && session2 == null) {
            return;
        }

        // 4. 发通知
        if (session1 != null) {
            WebSocketUtils.sendMessage(session1, WebSocketResult.success(response));
        }
        if (session2 != null) {
            WebSocketUtils.sendMessage(session2, WebSocketResult.success(response));
        }

        // 5. 如果有人赢了就销毁房间，更新数据库
        if (response.getWinner() != 0) {
            log.info("[GameAPI::handleTextMessage] 销毁房间，更新数据库");
            gameService.handleWinner(room, response.getWinner());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        User user = (User) session.getAttributes().get(Constants.SESSION_KEY);
        if (user == null) {
            log.warn("[GameAPI::afterConnectionClosed] 用户未登录");
            return;
        }
        WebSocketSession exitSession = onlineUserManger.getFromGameRoom(user.getUserId());
        if (session == exitSession) {
            onlineUserManger.exitGameRoom(user.getUserId());
            log.info("[GameAPI::afterConnectionClosed] 用户已退出游戏房间");
        }
        notifyTharUser(user);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        User user = (User) session.getAttributes().get(Constants.SESSION_KEY);
        if (user == null) {
            log.warn("[GameAPI::handleTransportError] 用户未登录");
            return;
        }
        WebSocketSession exitSession = onlineUserManger.getFromGameRoom(user.getUserId());
        if (session == exitSession) {
            onlineUserManger.exitGameRoom(user.getUserId());
            log.warn("[GameAPI::afterConnectionClosed] 用户链接异常，已清除出游戏房间");
        }
        notifyTharUser(user);
    }

    private void notifyTharUser(User user) throws IOException {
        // 1. 判断房间还在不在
        Room room = roomManger.getRoomByUserId(user.getUserId());
        if (room == null) {
            log.warn("[GameAPI::notifyTharUser] 房间为空，可能在其他地方进行了更新操作，不做处理");
            return;
        }
        // 2. 判断玩家还在不在
        User tharUser = (user == room.getUser1() ? room.getUser2() : room.getUser1());
        WebSocketSession session = onlineUserManger.getFromGameRoom(tharUser.getUserId());
        if (session == null) {
            log.warn("玩家{}也下线了，平局", tharUser.getUserId());
            return;
        }
        // 3. 发消息
        GameResponse response = new GameResponse();
        response.setMessage(Constants.PUT_CHESS);
        response.setUserId(tharUser.getUserId());
        response.setWinner(tharUser.getUserId());
        WebSocketUtils.sendMessage(session, WebSocketResult.success(response));
        // 4. 更新数据库
        int winnerId = tharUser.getUserId();
        gameService.handleWinner(room, winnerId);
    }
}
