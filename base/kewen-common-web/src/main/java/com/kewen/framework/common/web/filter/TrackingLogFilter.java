package com.kewen.framework.common.web.filter;

import com.kewen.framework.common.context.ContextConstant;
import com.kewen.framework.common.context.TraceContext;
import com.kewen.framework.common.core.utils.UUIDUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *  全局跟踪号过滤器
 *  spring security自带的过滤器的order为-100，因此在spring之前的需要大于此，因此设定在 -10000 ~ -1000
 * @author kewen
 * @since 2023-04-26
 */
@Slf4j
@Order(-9999)
public class TrackingLogFilter extends OncePerRequestFilter {


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //从请求中取得交易号，没有则生成一个
        String traceId = request.getHeader(ContextConstant.TRACE_ID_KEY);
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
