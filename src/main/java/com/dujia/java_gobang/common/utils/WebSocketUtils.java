package com.dujia.java_gobang.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Slf4j
public class WebSocketUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public WebSocketUtils() {

    }

    /**
     * 发送WebSocket消息
     *
     * @param session 会话
     * @param message 消息
     * @throws IOException 异常
     */
    public static void sendMessage(WebSocketSession session, Object message) throws IOException {
        try {
            if (session != null && session.isOpen()) {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
            }
        } catch (Exception e) {
            log.error("[WebSocketUtils::sendMessage] 消息发送失败", e);
        }
    }
}
