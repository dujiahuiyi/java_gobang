package com.dujia.java_gobang.model.result;

import com.dujia.java_gobang.common.ResultCode;
import lombok.Data;

@Data
public class WebSocketResult<T> {
    private ResultCode code;
    private String errMsg;
    private T data;

    public static <T>WebSocketResult<T> success(T data) {
        WebSocketResult<T> result = new WebSocketResult<>();
        result.setCode(ResultCode.SUCCESS);
        result.setData(data);
        return result;
    }

    public static <T>WebSocketResult<T> fail(String errMsg, T data) {
        WebSocketResult<T> result = new WebSocketResult<>();
        result.setCode(ResultCode.FAIL);
        result.setErrMsg(errMsg);
        result.setData(data);
        return result;
    }

    public static <T>WebSocketResult<T> fail(String errMsg) {
        WebSocketResult<T> result = new WebSocketResult<>();
        result.setCode(ResultCode.FAIL);
        result.setErrMsg(errMsg);
        return result;
    }

    public static <T>WebSocketResult<T> fail(T data) {
        WebSocketResult<T> result = new WebSocketResult<>();
        result.setCode(ResultCode.FAIL);
        result.setData(data);
        return result;
    }
}
