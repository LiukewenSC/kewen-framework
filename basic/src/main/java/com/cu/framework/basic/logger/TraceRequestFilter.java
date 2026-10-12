package com.cu.framework.basic.logger;


import com.cu.framework.basic.filter.EarlyRequestFilter;
import com.cu.framework.basic.logger.trace.HeaderTraceIdProcessor;
import com.cu.framework.basic.logger.trace.TraceContext;
import com.cu.framework.basic.logger.trace.TraceIdProcessor;
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

    /** 流水号处理器，容器中无 TraceIdProcessor 时默认为基于请求头的 HeaderTraceIdProcessor */
    private TraceIdProcessor traceIdProcessor;

    /**
     * 构造过滤器，优先使用容器中的 TraceIdProcessor，未注册时回退到 HeaderTraceIdProcessor
     * @param traceIdProcessor 容器中 TraceIdProcessor 的 ObjectProvider
     */
    public TraceRequestFilter(ObjectProvider<TraceIdProcessor> traceIdProcessor) {
        TraceIdProcessor processor = traceIdProcessor.getIfAvailable();
        if (processor == null) {
            this.traceIdProcessor = new HeaderTraceIdProcessor();
        } else {
            this.traceIdProcessor = processor;
        }
    }

    /**
     * 通过流水号处理器获取本次请求的流水号并写入 TraceContext（MDC），
     * 放行后续过滤器链，请求处理结束后在 finally 中调用 TraceContext.remove(traceId)
     */
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
