package com.cu.framework.boot.auth.core;

import com.cu.framework.auth.core.exception.AuthException;
import com.cu.framework.auth.core.exception.AuthServiceException;
import com.cu.framework.basic.model.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
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

    /**
     * 处理调用方式错误 AuthServiceException，返回 code 400 的失败结果。
     * 与 403 分开是为了让调用方不必解析消息文本就能判出"当前登录人无权"与"这次调用无法判定"
     */
    @ExceptionHandler(AuthServiceException.class)
    public Result<Void> authServiceException(AuthServiceException e){
        logger.error(e.getMessage(),e);
        return Result.failed(HttpStatus.BAD_REQUEST.value(),e.getMessage());
    }
}
