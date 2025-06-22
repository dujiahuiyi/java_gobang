package com.dujia.java_gobang.game;

import lombok.Data;

@Data
public class GameReadResponse {
    private String message;
    private boolean ok;
    private String reason;
    private String roomId;
    private int thisUserId;
    private int thatUserId;
    private int whiteUserId;
}
