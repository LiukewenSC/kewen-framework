package com.kewen.framework.common.logger;

import com.kewen.framework.common.core.filter.EarlyRequestFilter;
import com.kewen.framework.common.core.utils.UUIDUtil;
import com.kewen.framework.common.logger.model.LoggerConstant;
import com.kewen.framework.common.logger.context.TraceContext;
import lombok.extern.slf4j.Slf4j;
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
@Order(3)
public class TraceRequestFilter implements EarlyRequestFilter {

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //从请求中取得交易号，没有则生成一个
        String traceId = request.getHeader(LoggerConstant.TRACE_ID_KEY);
        if (traceId==null){
            traceId = UUIDUtil.generate();
        }
        TraceContext.set(traceId);
        try {
            filterChain.doFilter(request,response);
        } finally {
            TraceContext.remove(traceId);
        }
    }
}
