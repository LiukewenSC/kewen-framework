package com.kewen.framework.common.logger.trace;


import com.kewen.framework.common.core.utils.UUIDUtil;
import com.kewen.framework.common.logger.LoggerConstant;

import javax.servlet.http.HttpServletRequest;

/**
 *  基于请求头的请求头流水号处理器
 * @author kewen
 * @since 2024-08-29
 */
public class HeaderTraceIdProcessor implements TraceIdProcessor {
    @Override
    public String getTraceId(HttpServletRequest request) {
        //从请求中取得交易号，没有则生成一个
        String traceId = request.getHeader(LoggerConstant.TRACE_ID_KEY);
        if (traceId==null){
            traceId = UUIDUtil.generate();

        }
        return traceId;
    }
}
