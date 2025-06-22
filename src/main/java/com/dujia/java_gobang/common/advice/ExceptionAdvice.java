package com.dujia.java_gobang.common.advice;

import com.dujia.java_gobang.exceptions.GoBangException;
import com.dujia.java_gobang.model.result.HttpResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ExceptionAdvice {
    @ExceptionHandler
    public void handler(Exception e) {
        log.error("发生异常：e", e);
    }

    @ExceptionHandler
    public HttpResult<Object> handlerGoBangExceptionHandler(GoBangException e) {
        return HttpResult.fail(e.getMessage());
    }

}
