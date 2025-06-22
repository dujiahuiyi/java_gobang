package com.dujia.java_gobang.game;

import lombok.Data;

@Data
public class GameRequest {
    private String message;
    private int userId;
    private int row;
    private int col;
}
