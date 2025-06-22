package com.dujia.java_gobang.game;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class RoomManger {

    // RoodId -> room
    private final ConcurrentHashMap<String, Room> rooms = new ConcurrentHashMap<>();
    // UserId -> RoomId
    private final ConcurrentHashMap<Integer, String> userIdToRoomId = new ConcurrentHashMap<>();

    public void add(Room room, Integer userId1, Integer userId2) {
        rooms.put(room.getRoomId(), room);
        userIdToRoomId.put(userId1, room.getRoomId());
        userIdToRoomId.put(userId2, room.getRoomId());
    }

    public void remove(String roomId, Integer userId1, Integer userId2) {
        rooms.remove(roomId);
        userIdToRoomId.remove(userId1);
        userIdToRoomId.remove(userId2);
    }

    public Room getRoomByRoomId(String roomId) {
        return rooms.get(roomId);
    }

    public Room getRoomByUserId(Integer userId) {
        String roomId = userIdToRoomId.get(userId);
        if (roomId == null) {
            return null;
        }
        return rooms.get(roomId);
    }
}
