package com.kewen.framework.auth.handler;

import com.kewen.framework.auth.core.exception.AuthException;
import com.kewen.framework.auth.security.exception.NoLoginException;
import com.kewen.framework.basic.model.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionAdviceHandler {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdviceHandler.class);


    @ExceptionHandler(AccessDeniedException.class)
    public Result accessDeniedException(AccessDeniedException t){
        logger.error("访问异常：{}", t.getMessage(), t);
        return Result.failed(401, t.getMessage());
    }


    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(NoLoginException.class)
    public Result authenticationException(NoLoginException t){
        logger.error("服务异常：{}", t.getMessage(), t);
        return Result.failed(401, t.getMessage());
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AuthenticationException.class)
    public Result authenticationException(AuthenticationException t){
        logger.error("认证异常：{}", t.getMessage(), t);
        return Result.failed(403, t.getMessage());
    }

    @ExceptionHandler(AuthException.class)
    public Result authException(AuthException e){
        logger.error(e.getMessage(),e);
        return Result.failed(HttpStatus.FORBIDDEN.value(),e.getMessage());
    }


    @ExceptionHandler(Throwable.class)
    public Result throwException(Throwable t){
        logger.error("全局异常：{}", t.getMessage(), t);
        return Result.failed(500, t.getMessage());
    }

    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public Result authenticationException(InternalAuthenticationServiceException t){
        logger.error("服务异常：{}", t.getMessage(), t);
        return Result.failed(500, t.getMessage());
    }

    @ExceptionHandler(InsufficientAuthenticationException.class)
    public Result insufficientAuthenticationException(InsufficientAuthenticationException t){
        logger.error("请求的权限不足，需要提升：{}", t.getMessage(), t);
        return Result.failed(401, "请求的权限不足，需要提升，请重新登录");
    }

}
