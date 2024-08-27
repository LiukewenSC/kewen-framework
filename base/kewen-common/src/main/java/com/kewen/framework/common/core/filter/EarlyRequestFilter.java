package com.kewen.framework.common.core.filter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 在请求进入时最开始的过滤器，用于处理日志记录等
 * @author kewen
 * @since 2024-07-10
 */
public interface EarlyRequestFilter {

    /**
     * 执行过滤器
     * @param request
     * @param response
     * @param filterChain
     */
    void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException;
}
