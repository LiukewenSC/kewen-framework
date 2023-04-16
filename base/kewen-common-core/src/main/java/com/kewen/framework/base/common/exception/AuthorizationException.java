package com.kewen.framework.base.common.exception;

/**
 * @descrpition 授权异常
 * @author kewen
 * @since 2022-11-25 9:49
 */
public class AuthorizationException extends BaseException {

    public AuthorizationException() {
    }

    public AuthorizationException(String message) {
        super(message);
    }

    public AuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthorizationException(Throwable cause) {
        super(cause);
    }

    public AuthorizationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
