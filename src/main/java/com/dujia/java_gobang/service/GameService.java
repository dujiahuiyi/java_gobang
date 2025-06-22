package com.dujia.java_gobang.service;

import com.dujia.java_gobang.game.GameResponse;
import com.dujia.java_gobang.game.Room;
import com.dujia.java_gobang.model.dataobject.User;
import com.dujia.java_gobang.model.result.GameConnectionResult;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface GameService {

    /**
     * 处理玩家进入游戏房间的连接
     *
     * @param user 当前用户
     * @return 一个结果对象，指导Controller如何响应
     */
    GameConnectionResult handleConnectionEstablished(User user);

    /**
     * 处理玩家落子
     *
     * @param room    房间
     * @param payload WebSocket报文
     * @return 结果
     * @throws JsonProcessingException 异常
     */
    GameResponse handlePutChess(Room room, String payload) throws JsonProcessingException;

    /**
     * 销毁房间，更新数据库
     *
     * @param room     房间
     * @param winnerId 赢家id
     */
    void handleWinner(Room room, int winnerId);
}
