package com.kewen.framework.basic.logger;


import com.kewen.framework.basic.filter.EarlyRequestFilter;
import com.kewen.framework.basic.logger.trace.HeaderTraceIdProcessor;
import com.kewen.framework.basic.logger.trace.TraceContext;
import com.kewen.framework.basic.logger.trace.TraceIdProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.annotation.Order;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *  全局跟踪号过滤器
 * @author kewen
 * @since 2023-04-26
 */
@Slf4j
@Order(1)
public class TraceRequestFilter implements EarlyRequestFilter {

    private TraceIdProcessor traceIdProcessor;

    public TraceRequestFilter(ObjectProvider<TraceIdProcessor> traceIdProcessor) {
        TraceIdProcessor processor = traceIdProcessor.getIfAvailable();
        if (processor == null) {
            this.traceIdProcessor = new HeaderTraceIdProcessor();
        } else {
            this.traceIdProcessor = processor;
        }
    }

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        //获取请求头流水号
        String traceId = traceIdProcessor.getTraceId(request);

        TraceContext.set(traceId);
        try {
            filterChain.doFilter(request,response);
        } finally {
            TraceContext.remove(traceId);
        }
    }
}
