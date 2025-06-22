package com.dujia.java_gobang.common;

public enum ResultCode {
    SUCCESS(200), // 正常响应
    FAIL(-1); // 错误响应

    private final Integer code;

    private ResultCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
