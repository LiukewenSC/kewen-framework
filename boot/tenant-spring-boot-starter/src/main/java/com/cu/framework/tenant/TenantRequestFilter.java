package com.cu.framework.tenant;


import com.cu.framework.basic.filter.EarlyRequestFilter;
import org.springframework.core.annotation.Order;
import org.springframework.util.StringUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 
 * @author kewen
 * @since 2024-08-27
 */
@Order(2)
public class TenantRequestFilter implements EarlyRequestFilter {

    /**
     * 从请求头 tenantId 中解析租户 ID 并写入 TenantContext，请求处理完成后清除；
     * 请求头为空时直接放行，不设置租户上下文
     * @param request 请求
     * @param response 响应
     * @param filterChain 过滤器链
     * @throws IOException IO 异常
     * @throws ServletException Servlet 异常
     */
    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        String tenantStr = request.getHeader(TenantConstant.TENANT_ID);
        if (StringUtils.hasText(tenantStr)){
            try {
                TenantContext.set(Long.parseLong(tenantStr));
                filterChain.doFilter(request, response);
            } finally {
                TenantContext.remove();
            }
        } else {
            filterChain.doFilter(request, response);
        }

    }
}
