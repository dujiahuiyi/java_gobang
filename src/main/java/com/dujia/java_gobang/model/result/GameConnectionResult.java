package com.dujia.java_gobang.model.result;

import com.dujia.java_gobang.game.GameReadResponse;
import lombok.Data;

/**
 * 定义一个结果类，告诉GameAPI下一步该做什么
 */
@Data
public class GameConnectionResult {
    private boolean needsToNotifyAll; // 是否需要通知房间里的其他玩家
    private GameReadResponse notificationPayload; // 需要发送的统一消息
    private GameReadResponse singUserPayload; // 当前用户的消息体
}
