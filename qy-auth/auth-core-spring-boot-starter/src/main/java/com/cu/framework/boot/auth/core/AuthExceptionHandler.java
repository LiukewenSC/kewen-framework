package com.cu.framework.boot.auth.core;

import com.cu.framework.auth.core.exception.AuthException;
import com.cu.framework.basic.model.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(AuthExceptionHandler.class);

    /**
     * 处理权限框架业务异常 AuthException，返回 code 403 的失败结果
     */
    @ExceptionHandler(AuthException.class)
    public Result<Void> authException(AuthException e){
        logger.error(e.getMessage(),e);
        return Result.failed(HttpStatus.FORBIDDEN.value(),e.getMessage());
    }
}
