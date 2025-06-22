package com.dujia.java_gobang.constant;

public class Constants {
    // URL
    public static final String LOGIN = "/login";
    public static final String REGISTER = "/register";
    public static final String USERINFO = "/userInfo";
    // Service
    public static final String USER_SERVICE = "userService";
    public static final String MATCHER_SERVICE = "matcherService";
    // 默认天梯分数
    public static final int SCORE = 1000;
    // sessionKey
    public static final String SESSION_KEY = "sessionKey";
    // 游戏准备就绪
    public static final String GAME_READY = "gameReady";
    // 下棋
    public static final String PUT_CHESS = "putChess";
    // 棋盘大小
    public static final int MAX_ROW = 15;
    public static final int MAX_COL = 15;
    // 匹配
    public static final String START_MATCH = "startMatch";
    public static final String STOP_MATCH = "stopMatch";
    public static final String REPEAT_CONNECTION = "repeatConnection"; // 多开
}
