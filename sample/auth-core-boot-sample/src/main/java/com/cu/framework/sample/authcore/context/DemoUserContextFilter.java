package com.cu.framework.sample.authcore.context;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 身份过滤器：在任何 MVC 拦截器之前把请求头里的身份与追踪标识写进 {@link DemoUserContext}。
 * <p>
 * 用 Filter 而不是 HandlerInterceptor 是有意的：菜单鉴权本身就是一个 HandlerInterceptor，
 * 而多个 Interceptor 之间的先后顺序在配置层没有可靠保证，身份必须先于它就绪
 *
 * @author kewen
 * @since 2026-10-20
 */
@Order(1)
@Component
public class DemoUserContextFilter extends OncePerRequestFilter {

    /**
     * 解析请求头建立上下文，请求结束后无条件清理
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            DemoUserContext.set(trimToNull(request.getHeader(DemoUserContext.USER_HEADER)),
                    trimToNull(request.getHeader(DemoUserContext.TRACE_HEADER)));
            filterChain.doFilter(request, response);
        } finally {
            DemoUserContext.clear();
        }
    }

    /**
     * 空串与纯空白按"未提供"处理，避免误当成一个存在的用户
     *
     * @param value 请求头原始值
     * @return 去空白后的值，空则返回 null
     */
    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
