package com.kewen.framework.common.web.interceptor;

import com.kewen.framework.common.context.TenantContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;

/**
 *  租户拦截器
 * @author kewen
 * @since 2023-04-17
 */
public class TenantInterceptor implements WebRequestInterceptor {

    private final boolean isOpenTenant;

    public TenantInterceptor(boolean isOpenTenant) {
        this.isOpenTenant=isOpenTenant;
    }

    @Override
    public void preHandle(WebRequest webRequest) throws Exception {
        if (isOpenTenant){
            String tenantStr = webRequest.getHeader("tenantId");
            if (StringUtils.isNotBlank(tenantStr)){
                TenantContext.set((Long.parseLong(tenantStr)));
            }

        }
    }

    @Override
    public void postHandle(WebRequest request, ModelMap model) throws Exception{

    }

    @Override
    public void afterCompletion(WebRequest webRequest, Exception e) throws Exception {
        if (isOpenTenant){
            TenantContext.clear();
        }
    }
}
