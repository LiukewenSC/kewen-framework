package com.cu.framework.boot.auth.core;

import com.cu.framework.auth.core.exception.AuthException;
import com.cu.framework.auth.core.exception.AuthServiceException;
import com.cu.framework.basic.exception.ErrorLog;
import com.cu.framework.basic.exception.ErrorType;
import com.cu.framework.basic.model.Result;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
public class AuthExceptionHandler {

    /**
     * 处理权限框架业务异常 AuthException，HTTP 状态与结果 code 均为 403
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AuthException.class)
    public Result<Void> authException(AuthException e, HttpServletRequest request){
        ErrorLog.warn(ErrorType.AUTHORIZATION, request, "权限拒绝：" + e.getMessage());
        return Result.failed(ErrorType.AUTHORIZATION, e.getMessage());
    }

    /**
     * 处理调用方式错误 AuthServiceException，HTTP 状态与结果 code 均为 400。
     * 与 403 分开是为了让调用方不必解析消息文本就能判出"当前登录人无权"与"这次调用无法判定"
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(AuthServiceException.class)
    public Result<Void> authServiceException(AuthServiceException e, HttpServletRequest request){
        ErrorLog.warn(ErrorType.USAGE, request, "调用方式错误：" + e.getMessage());
        return Result.failed(ErrorType.USAGE, e.getMessage());
    }
}
