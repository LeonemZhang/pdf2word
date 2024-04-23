package com.zzsn.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * @author 张宗涵
 * @date 2024/4/20
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler
    public String handleException(Exception e) {
        log.error("捕获到异常：", e);
        return e.getMessage();
    }
}
