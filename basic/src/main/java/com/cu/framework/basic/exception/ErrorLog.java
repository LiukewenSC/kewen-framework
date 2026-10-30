package com.cu.framework.basic.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * 错误分类日志：固定输出分类标记、请求方法与路径，预期失败不留堆栈，服务端故障保留堆栈
 * @author kewen
 * @since 2026-10-30
 */
public final class ErrorLog {

    private static final Logger logger = LoggerFactory.getLogger(ErrorLog.class);

    private ErrorLog() {
    }

    /**
     * 记录预期内的认证状态提示，不打印堆栈
     */
    public static void info(ErrorType errorType, HttpServletRequest request, String detail) {
        logger.info(line(errorType, request, detail));
    }

    /**
     * 记录预期内的调用方失败，不打印堆栈
     */
    public static void warn(ErrorType errorType, HttpServletRequest request, String detail) {
        logger.warn(line(errorType, request, detail));
    }

    /**
     * 记录服务端故障，保留完整堆栈
     */
    public static void error(ErrorType errorType, HttpServletRequest request, String detail, Throwable throwable) {
        logger.error(line(errorType, request, detail), throwable);
    }

    private static String line(ErrorType errorType, HttpServletRequest request, String detail) {
        String method = request == null ? "-" : request.getMethod();
        String path = request == null ? "-" : request.getRequestURI();
        return "[" + errorType.name() + "] " + method + " " + path + " - " + detail;
    }
}
