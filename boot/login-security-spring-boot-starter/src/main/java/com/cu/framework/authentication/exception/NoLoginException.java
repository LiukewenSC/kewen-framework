package com.cu.framework.authentication.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * 未登录异常，继承 Spring Security 的 AuthenticationException，
 * 由 ExceptionAdviceHandler 捕获后以 401 状态返回未登录提示
 * @author kewen
 * @since 2026-02-12
 */
public class NoLoginException extends AuthenticationException {

    public NoLoginException(String msg, Throwable t) {
        super(msg, t);
    }

    public NoLoginException(String msg) {
        super(msg);
    }
}
