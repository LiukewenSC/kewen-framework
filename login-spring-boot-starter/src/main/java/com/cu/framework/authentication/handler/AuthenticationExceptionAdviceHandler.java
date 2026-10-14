package com.cu.framework.authentication.handler;

import com.cu.framework.authentication.exception.NoLoginException;
import com.cu.framework.basic.model.Result;
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

/**
 * 全局异常处理增强（@RestControllerAdvice），将访问拒绝、未登录、认证失败等安全异常
 * 及兜底的未知异常统一转换为 Result JSON 响应
 * @author kewen
 * @since 2026-02-12
 */
@RestControllerAdvice
public class AuthenticationExceptionAdviceHandler {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationExceptionAdviceHandler.class);


    /**
     * 处理访问拒绝异常，返回 code 401 的失败结果
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Result<Void> accessDeniedException(AccessDeniedException t){
        logger.error("访问异常：{}", t.getMessage(), t);
        return Result.failed(401, t.getMessage());
    }


    /**
     * 处理未登录异常，HTTP 状态与结果 code 均为 401
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(NoLoginException.class)
    public Result<Void> authenticationException(NoLoginException t){
        logger.error("服务异常：{}", t.getMessage(), t);
        return Result.failed(401, t.getMessage());
    }

    /**
     * 处理其余认证异常，HTTP 状态与结果 code 均为 403
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AuthenticationException.class)
    public Result<Void> authenticationException(AuthenticationException t){
        logger.error("认证异常：{}", t.getMessage(), t);
        return Result.failed(403, t.getMessage());
    }



    /**
     * 兜底处理所有未匹配的异常，返回 code 500 的失败结果
     */
    @ExceptionHandler(Throwable.class)
    public Result<Void> throwException(Throwable t){
        logger.error("全局异常：{}", t.getMessage(), t);
        return Result.failed(500, t.getMessage());
    }

    /**
     * 处理认证服务内部异常，返回 code 500 的失败结果
     */
    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public Result<Void> authenticationException(InternalAuthenticationServiceException t){
        logger.error("服务异常：{}", t.getMessage(), t);
        return Result.failed(500, t.getMessage());
    }

    /**
     * 处理认证信息不足异常，返回 code 401 并提示重新登录
     */
    @ExceptionHandler(InsufficientAuthenticationException.class)
    public Result<Void> insufficientAuthenticationException(InsufficientAuthenticationException t){
        logger.error("请求的权限不足，需要提升："+ t.getMessage(), t);
        return Result.failed(401, "请求的权限不足，需要提升，请重新登录");
    }

}
