package com.dujia.java_gobang.model.result;

import com.dujia.java_gobang.common.ResultCode;
import lombok.Data;

@Data
public class HttpResult<T> {
    private ResultCode resultCode;
    private String errMsg;
    private T date;

    public static <T> HttpResult<T> success(T date) {
        HttpResult<T> httpResult = new HttpResult<>();
        httpResult.setResultCode(ResultCode.SUCCESS);
        httpResult.setDate(date);
        return httpResult;
    }

    public static <T> HttpResult<T> fail(String errMsg) {
        HttpResult<T> httpResult = new HttpResult<>();
        httpResult.setResultCode(ResultCode.FAIL);
        httpResult.setErrMsg(errMsg);
        return httpResult;
    }

    public static <T> HttpResult<T> fail(String errMsg, T date) {
        HttpResult<T> httpResult = new HttpResult<>();
        httpResult.setResultCode(ResultCode.FAIL);
        httpResult.setErrMsg(errMsg);
        httpResult.setDate(date);
        return httpResult;
    }
}
