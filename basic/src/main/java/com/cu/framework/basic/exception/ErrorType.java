package com.cu.framework.basic.exception;

import org.springframework.dao.DataAccessException;

/**
 * 错误分类：统一承载分类到 HTTP 状态与业务码的映射，并负责识别被包装的数据库异常
 * @author kewen
 * @since 2026-10-30
 */
public enum ErrorType {

    /** 未登录、凭据错误、账号状态异常、会话失效 */
    AUTHENTICATION(401),
    /** 已认证但无权限 */
    AUTHORIZATION(403),
    /** 参数校验、绑定、解析与类型转换失败 */
    VALIDATION(400),
    /** 业务规则失败 */
    BUSINESS(400),
    /** 调用方式错误 */
    USAGE(400),
    /** 数据库访问失败 */
    DATABASE(500),
    /** 未预期异常 */
    SYSTEM(500);

    private final int status;

    ErrorType(int status) {
        this.status = status;
    }

    /**
     * 该分类对应的 HTTP 状态与业务码
     */
    public int status() {
        return status;
    }

    /**
     * 是否为服务端故障，决定日志级别与是否保留堆栈
     */
    public boolean serverFault() {
        return status >= 500;
    }

    /**
     * 沿 cause 链判断异常是否由数据库访问失败引起
     * @param throwable 待判断的异常
     * @return 自身或任一 cause 为数据库访问异常时返回 true
     */
    public static boolean causedByDatabase(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof DataAccessException) {
                return true;
            }
            Throwable cause = current.getCause();
            if (cause == current) {
                return false;
            }
            current = cause;
        }
        return false;
    }
}
