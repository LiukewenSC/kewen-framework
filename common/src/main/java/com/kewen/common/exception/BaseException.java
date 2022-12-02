package com.kewen.common.exception;

/**
 * @descrpition 基础异常
 * @author kewen
 * @since 2022-11-28 14:16
 */
public class BaseException extends RuntimeException{

    protected Integer code = 500;

    public BaseException() {
    }

    public BaseException(String message) {
        super(message);
    }

    public BaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public BaseException(Throwable cause) {
        super(cause);
    }

    public BaseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
