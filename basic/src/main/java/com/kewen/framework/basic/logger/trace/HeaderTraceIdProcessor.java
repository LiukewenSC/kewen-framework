package com.kewen.framework.basic.logger.trace;



import com.kewen.framework.basic.logger.LoggerConstant;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

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
            traceId = UUID.randomUUID().toString().replaceAll("-","");

        }
        return traceId;
    }
}
