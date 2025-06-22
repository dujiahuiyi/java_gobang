package com.dujia.java_gobang.game;

import com.dujia.java_gobang.constant.Constants;
import com.dujia.java_gobang.model.dataobject.User;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
@Data
public class Room {
    private String roomId;
    private User user1;
    private User user2;
    private int whiteUserId;
    private int maxRow = Constants.MAX_ROW;
    private int maxCol = Constants.MAX_COL;
    private int[][] chessBoard = new int[maxRow][maxCol];  // 未下的位置为0，user1下的位置为1，user2下的位置为2
    private volatile boolean gameOver;

    public Room() {
        this.roomId = UUID.randomUUID().toString();
    }

    public synchronized boolean isGameOver() {
        return this.gameOver;
    }

    /**
     * 设置游戏是否结束，并返回之前是否已经结束了
     *
     * @return true - 之前就结束了 false - 之前还没结束，当前已设置为结束
     */
    public synchronized boolean setGameOver() {
        if (this.gameOver) {
            return true;
        }
        this.gameOver = true;
        return false;
    }
}
