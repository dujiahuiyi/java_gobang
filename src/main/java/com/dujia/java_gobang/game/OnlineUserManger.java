package com.dujia.java_gobang.game;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class OnlineUserManger {
    // 用户在游戏大厅的在线状态
    private final ConcurrentHashMap<Integer, WebSocketSession> gameHall = new ConcurrentHashMap<>();
    // 用户在匹配房间的在线状态
    private final ConcurrentHashMap<Integer, WebSocketSession> gameRoom = new ConcurrentHashMap<>();

    public void enterGameHall(Integer userId, WebSocketSession session) {
        gameHall.put(userId, session);
    }

    public void exitGameHall(Integer userId) {
        gameHall.remove(userId);
    }

    public WebSocketSession getFromGameHall(Integer userId) {
        return gameHall.get(userId);
    }

    public void enterGameRoom(Integer userId, WebSocketSession session) {
        gameRoom.put(userId, session);
    }

    public void exitGameRoom(Integer userId) {
        gameRoom.remove(userId);
    }

    public WebSocketSession getFromGameRoom(Integer userId) {
        return gameRoom.get(userId);
    }
}
