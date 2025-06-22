package com.dujia.java_gobang.api;

import com.dujia.java_gobang.common.utils.WebSocketUtils;
import com.dujia.java_gobang.constant.Constants;
import com.dujia.java_gobang.game.OnlineUserManger;
import com.dujia.java_gobang.model.dataobject.User;
import com.dujia.java_gobang.model.request.MatchRequest;
import com.dujia.java_gobang.model.response.MatchResponse;
import com.dujia.java_gobang.model.result.MatchResult;
import com.dujia.java_gobang.model.result.WebSocketResult;
import com.dujia.java_gobang.service.MatcherService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;

@Slf4j
@Component
public class MatchAPI extends TextWebSocketHandler {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OnlineUserManger onlineUserManger;

    @Resource(name = Constants.MATCHER_SERVICE)
    private MatcherService matcherService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 将用户加入在线状态
        try {
            User user = (User) session.getAttributes().get(Constants.SESSION_KEY);
            log.info("[MatchAPI::afterConnectionEstablished] 当前登录的用户{}", user.getUserId());
            // 判断是否多开
            if (!matcherService.joinOnlineStatus(user.getUserId())) {
                val matchResponse = successResult("禁止多开", Constants.REPEAT_CONNECTION);
                WebSocketUtils.sendMessage(session, matchResponse);
                return;
            }

            // 加入在线状态
            // matcherService.jointTheOnlineStatus();
            onlineUserManger.enterGameHall(user.getUserId(), session);

        } catch (NullPointerException e) {
            log.warn("用户未登录");
            val matchResponse = failResult("您尚未登录! 不能进行后续匹配功能!");
            WebSocketUtils.sendMessage(session, matchResponse);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            // 处理匹配和取消匹配
            User user = (User) session.getAttributes().get(Constants.SESSION_KEY);
            log.info("[MatchAPI::handleTextMessage] 当前登录的用户{}", user.getUserId());
            String payload = message.getPayload();
            MatchRequest request = objectMapper.readValue(payload, MatchRequest.class);
            WebSocketResult<MatchResponse> response;
            String reqMessage = request.getMessage();
            switch (reqMessage) {
                case Constants.START_MATCH: {
                    MatchResult result = matcherService.addToMatchQueue(user.getUserId());
                    response = result.isSuccess() ? successResult(result.getMessage()) : failResult(result.getMessage());
                    break;
                }
                case Constants.STOP_MATCH: {
                    MatchResult result = matcherService.removeFromMatchQueue(user.getUserId());
                    response = result.isSuccess() ? successResult(result.getMessage()) : failResult(result.getMessage());
                    break;
                }
                default: {
                    response = failResult("非法的匹配请求");
                    break;
                }
            }
            WebSocketUtils.sendMessage(session, response);
        } catch (NullPointerException e) {
            log.warn("[MatchAPI::handleTextMessage] 用户未登录");
            val response = failResult("您尚未登录! 不能进行后续匹配功能!");
            WebSocketUtils.sendMessage(session, response);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 相当于玩家下线
        offLine(session);

    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        offLine(session);
    }

    private void offLine(WebSocketSession session) {
        try {
            User user = (User) session.getAttributes().get(Constants.SESSION_KEY);
            log.info("[MatchAPI::offLine] 玩家下线");
            WebSocketSession webSocketSession = onlineUserManger.getFromGameHall(user.getUserId());
            if (webSocketSession == session) {
                onlineUserManger.exitGameHall(user.getUserId());
            }
//            matcher.remove(user);
            matcherService.handleUserOffline(user.getUserId());
        } catch (Exception e) {
            log.warn("[MatchAPI::offLine] 当前用户未登录");
        }
    }

    private WebSocketResult<MatchResponse> successResult(String message) {
        MatchResponse matchResponse = new MatchResponse();
        matchResponse.setOk(true);
        matchResponse.setMessage(message);
        return WebSocketResult.success(matchResponse);
    }

    private WebSocketResult<MatchResponse> successResult(String reason, String message) {
        val matchResponse = new MatchResponse();
        matchResponse.setOk(true);
        matchResponse.setReason(reason);
        matchResponse.setMessage(message);
        return WebSocketResult.success(matchResponse);
    }

    private WebSocketResult<MatchResponse> failResult(String reason) {
        val matchResponse = new MatchResponse();
        matchResponse.setOk(false);
        matchResponse.setReason(reason);
        return WebSocketResult.fail(matchResponse);
    }
}
