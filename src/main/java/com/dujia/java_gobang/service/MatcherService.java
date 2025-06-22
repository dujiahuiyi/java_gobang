package com.dujia.java_gobang.service;

import com.dujia.java_gobang.model.result.MatchResult;

public interface MatcherService {
    /**
     * 用户加入在线状态
     *
     * @param userId 用户Id
     * @return true - 未在线  false - 已在线
     */
    boolean joinOnlineStatus(Integer userId);

    /**
     * 加入匹配队列
     *
     * @param userId 用户Id
     * @return result.true - 加入成功  result.false - 加入失败
     */
    MatchResult addToMatchQueue(Integer userId);

    /**
     * 移除匹配队列
     *
     * @param userId 用户Id
     * @return result.true - 移除成功  result.false - 移除失败
     */
    MatchResult removeFromMatchQueue(Integer userId);

    /**
     * 处理用户下线
     *
     * @param userId 用户Id
     */
    void handleUserOffline(Integer userId);
}
