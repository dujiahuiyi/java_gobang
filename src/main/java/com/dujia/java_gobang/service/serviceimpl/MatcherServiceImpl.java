package com.dujia.java_gobang.service.serviceimpl;

import com.dujia.java_gobang.constant.Constants;
import com.dujia.java_gobang.game.Matcher;
import com.dujia.java_gobang.game.OnlineUserManger;
import com.dujia.java_gobang.model.dataobject.User;
import com.dujia.java_gobang.model.result.MatchResult;
import com.dujia.java_gobang.service.MatcherService;
import com.dujia.java_gobang.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


@Slf4j
@Service(Constants.MATCHER_SERVICE)
public class MatcherServiceImpl implements MatcherService {

    @Autowired
    private OnlineUserManger onlineUserManger;
    @Resource(name = "userService")
    private UserService userService;
    @Autowired
    private Matcher matcher;

    @Override
    public boolean joinOnlineStatus(Integer userId) {
        try {
            if (isUserOnline(userId)) {
                log.warn("[MatcherServiceImpl::joinOnlineStatus] 用户{}已在线", userId);
                return false;
            }
            return true; // 用户不在线，不做处理
        } catch (Exception e) {
            log.error("[MatcherServiceImpl::joinOnlineStatus] 用户{}加入在线状态失败", userId);
            return false;
        }
    }

    @Override
    public MatchResult addToMatchQueue(Integer userId) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return new MatchResult(false, "为找到用户");
            }
            matcher.add(user);
            log.info("[MatcherServiceImpl::addToMatchQueue] 用户{}加入匹配队列成功", userId);
            return new MatchResult(true, Constants.START_MATCH);
        } catch (Exception e) {
            log.error("[MatcherServiceImpl::addToMatchQueue] 添加用户{}到匹配队列失败", userId);
            return new MatchResult(false, "添加用户到匹配队列失败");
        }
    }

    @Override
    public MatchResult removeFromMatchQueue(Integer userId) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                log.warn("[MatcherServiceImpl::removeFromMatchQueue] 用户{}不存在，无需删除", userId);
                return new MatchResult(false, "用户不存在");
            }
            matcher.remove(user);
            log.warn("[MatcherServiceImpl::removeFromMatchQueue] 用户{}已删除", userId);
            return new MatchResult(true, Constants.STOP_MATCH);
        } catch (Exception e) {
            log.warn("[MatcherServiceImpl::removeFromMatchQueue] 从匹配队列删除用户{}失败", userId);
            return new MatchResult(false, "从匹配队列删除用户失败");
        }
    }

    @Override
    public void handleUserOffline(Integer userId) {
        try {
            User user = userService.getUserById(userId);
            if (user != null) {
                matcher.remove(user);
                log.info("[MatcherServiceImpl::handleUserOffline] 用户{}已下线", userId);
            }
        } catch (Exception e) {
            log.error("[MatcherServiceImpl::handleUserOffline] 用户{}下线出错", userId);
        }
    }

    private boolean isUserOnline(Integer userId) {
        return (onlineUserManger.getFromGameHall(userId) != null
                || onlineUserManger.getFromGameRoom(userId) != null);
    }
}
