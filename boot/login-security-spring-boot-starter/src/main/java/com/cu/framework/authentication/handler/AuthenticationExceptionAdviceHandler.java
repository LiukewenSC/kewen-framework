package com.cu.framework.authentication.handler;

import com.cu.framework.authentication.exception.NoLoginException;
import com.cu.framework.basic.exception.ErrorLog;
import com.cu.framework.basic.exception.ErrorType;
import com.cu.framework.basic.model.Result;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 全局异常处理增强（@RestControllerAdvice），将访问拒绝、未登录、认证失败等安全异常
 * 统一转换为 Result JSON 响应；不处理通用 Throwable，避免抢占权限与业务异常的具体处理器
 * @author kewen
 * @since 2026-02-12
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE + 200)
public class AuthenticationExceptionAdviceHandler {

    private static final String UNAUTHENTICATED_MESSAGE = "未登录或登录状态已失效，请重新登录";
    private static final String BAD_CREDENTIALS_MESSAGE = "用户名或密码错误";
    private static final String ACCESS_DENIED_MESSAGE = "没有访问该资源的权限";
    private static final String DATABASE_FAILED_MESSAGE = "数据库操作失败，请稍后重试";
    private static final String AUTH_SERVICE_FAILED_MESSAGE = "认证服务内部错误，请稍后重试";
    private static final String AUTHENTICATION_FAILED_MESSAGE = "认证失败，请重试";

    /**
     * 处理未登录异常，HTTP 状态与结果 code 均为 401
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(NoLoginException.class)
    public Result<Void> noLoginException(NoLoginException t, HttpServletRequest request){
        ErrorLog.info(ErrorType.AUTHENTICATION, request, "未登录：" + t.getMessage());
        return Result.failed(ErrorType.AUTHENTICATION, UNAUTHENTICATED_MESSAGE);
    }

    /**
     * 处理认证信息不足异常，通常为匿名访问受保护资源，HTTP 状态与结果 code 均为 401
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(InsufficientAuthenticationException.class)
    public Result<Void> insufficientAuthenticationException(InsufficientAuthenticationException t, HttpServletRequest request){
        ErrorLog.info(ErrorType.AUTHENTICATION, request, "未认证访问：" + t.getMessage());
        return Result.failed(ErrorType.AUTHENTICATION, UNAUTHENTICATED_MESSAGE);
    }

    /**
     * 处理其余认证异常，包括凭据错误与账号状态异常，HTTP 状态与结果 code 均为 401
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(AuthenticationException.class)
    public Result<Void> authenticationException(AuthenticationException t, HttpServletRequest request){
        ErrorLog.warn(ErrorType.AUTHENTICATION, request,
                t.getClass().getSimpleName() + ": " + t.getMessage());
        return Result.failed(ErrorType.AUTHENTICATION, authenticationMessage(t));
    }

    /**
     * 处理过滤器侧的认证服务用法错误，HTTP 状态与结果 code 均为 400
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(AuthenticationServiceException.class)
    public Result<Void> authenticationServiceException(AuthenticationServiceException t, HttpServletRequest request){
        ErrorLog.warn(ErrorType.USAGE, request,
                t.getClass().getSimpleName() + ": " + t.getMessage());
        return Result.failed(ErrorType.USAGE, t.getMessage());
    }

    /**
     * 处理访问拒绝异常，HTTP 状态与结果 code 均为 403
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public Result<Void> accessDeniedException(AccessDeniedException t, HttpServletRequest request){
        ErrorLog.warn(ErrorType.AUTHORIZATION, request, "权限拒绝：" + t.getMessage());
        return Result.failed(ErrorType.AUTHORIZATION, ACCESS_DENIED_MESSAGE);
    }

    /**
     * 处理认证服务内部异常：数据库访问失败归入 DATABASE，其余归入 SYSTEM
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public Result<Void> internalAuthenticationServiceException(InternalAuthenticationServiceException t, HttpServletRequest request){
        if (ErrorType.causedByDatabase(t)) {
            ErrorLog.error(ErrorType.DATABASE, request, "认证过程数据库访问失败", t);
            return Result.failed(ErrorType.DATABASE, DATABASE_FAILED_MESSAGE);
        }
        ErrorLog.error(ErrorType.SYSTEM, request, "认证服务内部错误", t);
        return Result.failed(ErrorType.SYSTEM, AUTH_SERVICE_FAILED_MESSAGE);
    }

    /**
     * 按认证异常的具体类型给出可区分的提示语义
     */
    private String authenticationMessage(AuthenticationException t){
        if (t instanceof BadCredentialsException) {
            return BAD_CREDENTIALS_MESSAGE;
        }
        if (t instanceof DisabledException) {
            return "账号已被禁用";
        }
        if (t instanceof LockedException) {
            return "账号已被锁定";
        }
        if (t instanceof AccountExpiredException) {
            return "账号已过期";
        }
        if (t instanceof CredentialsExpiredException) {
            return "登录凭证已过期";
        }
        return AUTHENTICATION_FAILED_MESSAGE;
    }

}
