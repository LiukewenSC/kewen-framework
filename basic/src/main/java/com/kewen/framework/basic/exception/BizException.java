package com.kewen.framework.basic.exception;

/**
 * @descrpition 业务异常
 * @author kewen
 * @since 2022-12-01 11:06
 */
public class BizException extends BackendException {
    public BizException() {
    }

    public BizException(String message) {
        super(message);
    }

    public BizException(String message, Throwable cause) {
        super(message, cause);
    }

    public BizException(Throwable cause) {
        super(cause);
    }

    public BizException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
