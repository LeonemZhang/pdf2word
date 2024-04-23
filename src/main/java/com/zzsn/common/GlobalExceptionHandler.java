package com.zzsn.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler
    public ResultVo<Void> handleException(Exception e) {
        log.error("捕获到异常：", e);
        return ResultVo.ofFailure(e.getMessage());
    }
}
