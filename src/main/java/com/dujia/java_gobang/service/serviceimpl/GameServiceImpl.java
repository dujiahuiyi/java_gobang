package com.dujia.java_gobang.service.serviceimpl;

import com.dujia.java_gobang.constant.Constants;
import com.dujia.java_gobang.game.*;
import com.dujia.java_gobang.mapper.UserMapper;
import com.dujia.java_gobang.model.dataobject.User;
import com.dujia.java_gobang.model.result.GameConnectionResult;
import com.dujia.java_gobang.service.GameService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Slf4j
@Service("gameService")
public class GameServiceImpl implements GameService {

    @Autowired
    private RoomManger roomManger;
    @Autowired
    private OnlineUserManger onlineUserManger;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public GameConnectionResult handleConnectionEstablished(User user) {
        GameConnectionResult result = new GameConnectionResult();
        GameReadResponse response = new GameReadResponse();

        if (user == null) {
            log.warn("[GameServiceImpl::handleConnectionEstablished] 用户未登录");
            response.setOk(false);
            response.setReason("用户未登录");
            result.setSingUserPayload(response);
            return result;
        }

        Room room = roomManger.getRoomByUserId(user.getUserId());
        if (room == null) {
            log.warn("[GameServiceImpl::handleConnectionEstablished] 房间为空");
            response.setOk(false);
            response.setReason("用户未匹配成功，不能进行游戏");
            result.setSingUserPayload(response);
            return result;
        }

        if (onlineUserManger.getFromGameHall(user.getUserId()) != null
                && onlineUserManger.getFromGameRoom(user.getUserId()) != null) {
            log.warn("[GameServiceImpl::handleConnectionEstablished] 用户{}多开", user.getUserId());
            response.setOk(false);
            response.setReason("禁止多开");
            result.setSingUserPayload(response);
            return result;
        }

        synchronized (room) {
            if (room.getUser1() == null) {
                log.warn("[GameServiceImpl::handleConnectionEstablished] 用户{}作为1号玩家加入房间", user.getUserId());
                room.setUser1(user);
                room.setWhiteUserId(user.getUserId());
                result.setNeedsToNotifyAll(false);
                return result;
            }

            if (room.getUser2() == null) {
                log.warn("[GameServiceImpl::handleConnectionEstablished] 用户{}作为2号玩家加入房间", user.getUserId());
                room.setUser2(user);

                response.setOk(true);
                response.setMessage(Constants.GAME_READY);
                response.setRoomId(room.getRoomId());
                response.setWhiteUserId(room.getWhiteUserId());
                result.setNeedsToNotifyAll(true);
                result.setNotificationPayload(response);
                return result;
            }
        }

        log.warn("[GameServiceImpl::handleConnectionEstablished] 多余用户{}登录", user.getUserId());
        response.setOk(false);
        response.setReason("房间已满");
        result.setSingUserPayload(response);
        return result;
    }

    @Override
    public GameResponse handlePutChess(Room room, String payload) throws JsonProcessingException {
        GameRequest request = objectMapper.readValue(payload, GameRequest.class);
        // 1. 谁下的棋
        int chess = (room.getUser1().getUserId() == request.getUserId() ? 1 : 2);
        log.info("[GameServiceImpl::handlePutChess] 玩家{}下棋", chess);
        int row = request.getRow();
        int col = request.getCol();
        int[][] chessBoard = room.getChessBoard();
        if (chessBoard[row][col] != 0) {
            log.warn("[GameServiceImpl::handlePutChess] 该位置{},{}已经有棋子", row, col);
            return null;
        }
        chessBoard[row][col] = chess;

        // 2. 有没有人赢
        GameResponse response = new GameResponse();
        // 赢家只能是当前玩家，因为这个棋子是他下的
        int winner = checkWinner(row, col, chess, chessBoard);
        if (winner != 0) {
            winner = (winner == 1 ? room.getUser1().getUserId() : room.getUser2().getUserId());
            log.info("[GameServiceImpl::handlePutChess] 赢家id{}", winner);
        }
        response.setMessage(Constants.PUT_CHESS);
        response.setUserId(request.getUserId());
        response.setCol(col);
        response.setRow(row);
        response.setWinner(winner);
        // 返回数据，不在这里使用session
        return response;
    }

    @Override
    public void handleWinner(Room room, int winnerId) {
        if (room == null) {
            log.warn("[GameServiceImpl::handleWinner] 房间为空，可能在其他地方进行了更新操作，不做处理");
            return;
        }
        if (room.setGameOver()) {
            log.warn("[GameServiceImpl::handleWinner] 房间 {} 游戏已经结束，不再重复处理。", room.getRoomId());
            return;
        }
        int loserId = (room.getUser1().getUserId() == winnerId ? room.getUser2().getUserId() : room.getUser1().getUserId());
        log.info("[GameServiceImpl::handleWinner] 赢家id{}, 输家id{}", winnerId, loserId);
        userMapper.updateWinner(winnerId);
        userMapper.updateLoser(loserId);
        roomManger.remove(room.getRoomId(), room.getUser1().getUserId(), room.getUser2().getUserId());
    }

    /**
     * 赢棋
     *
     * @param row   行
     * @param col   列
     * @param chess 下的棋
     * @return 谁赢返回谁的id，没人赢就返回0
     */
    private int checkWinner(int row, int col, int chess, int[][] chessBoard) {
        // 1. 判断行
        for (int c = col - 4; c <= col; c++) {
            try {
                if (chessBoard[row][c] == chess
                        && chessBoard[row][c + 1] == chess
                        && chessBoard[row][c + 2] == chess
                        && chessBoard[row][c + 3] == chess
                        && chessBoard[row][c + 4] == chess) {
                    return chess;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                continue;
            }
        }
        // 2. 判断列
        for (int r = row - 4; r <= row; r++) {
            try {
                if (chessBoard[r][col] == chess
                        && chessBoard[r + 1][col] == chess
                        && chessBoard[r + 2][col] == chess
                        && chessBoard[r + 3][col] == chess
                        && chessBoard[r + 4][col] == chess) {
                    return chess;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                continue;
            }
        }
        // 3. 判断左对角线
        for (int r = row - 4, c = col - 4; r <= row && c <= col; r++, c++) {
            try {
                if (chessBoard[r][c] == chess
                        && chessBoard[r + 1][c + 1] == chess
                        && chessBoard[r + 2][c + 2] == chess
                        && chessBoard[r + 3][c + 3] == chess
                        && chessBoard[r + 4][c + 4] == chess) {
                    return chess;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                continue;
            }
        }
        // 4. 判断右对角线
        for (int r = row - 4, c = col + 4; r <= row && c >= col; r++, c--) {
            try {
                if (chessBoard[r + 1][c - 1] == chess
                        && chessBoard[r + 2][c - 2] == chess
                        && chessBoard[r + 3][c - 3] == chess
                        && chessBoard[r + 4][c - 4] == chess
                        && chessBoard[r][c] == chess) {
                    return chess;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                continue;
            }
        }
        return 0;
    }
}
