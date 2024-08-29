package com.kewen.framework.common.logger.trace;


import javax.servlet.http.HttpServletRequest;

/**
 * 流水号生成器
 * @author kewen
 * @since 2024-08-29
 */
public interface TraceIdProcessor {
    /**
     * 流水号获取
     * @param request
     * @return
     */
    String getTraceId(HttpServletRequest request);
}
