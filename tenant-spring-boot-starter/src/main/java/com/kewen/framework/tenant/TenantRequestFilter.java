package com.kewen.framework.tenant;


import com.kewen.framework.basic.filter.EarlyRequestFilter;
import org.springframework.core.annotation.Order;
import org.springframework.util.StringUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 
 * @author kewen
 * @since 2024-08-27
 */
@Order(2)
public class TenantRequestFilter implements EarlyRequestFilter {

    private boolean isOpenTenant;

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        if (isOpenTenant){
            String tenantStr = request.getHeader("tenantId");
            if (!StringUtils.hasText(tenantStr)){
                try {
                    TenantContext.set(Long.parseLong(tenantStr));
                    filterChain.doFilter(request, response);
                } finally {
                    TenantContext.clear();
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
