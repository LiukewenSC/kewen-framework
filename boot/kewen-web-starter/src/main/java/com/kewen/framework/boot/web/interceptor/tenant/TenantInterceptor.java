package com.kewen.framework.boot.web.interceptor.tenant;

import com.kewen.framework.base.common.context.TenantContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;

/**
 * @descrpition 租户拦截器
 * @author kewen
 * @since 2022-12-05 10:07
 */
public interface TenantInterceptor extends WebRequestInterceptor {

    void setTenant(long tenantId);
    void clearTenant();

    @Override
    default void preHandle(WebRequest webRequest) throws Exception {
        String tenantStr = webRequest.getHeader("tenantId");
        if (StringUtils.isNotBlank(tenantStr)){
            setTenant(Long.parseLong(tenantStr));
        }
    }

    @Override
    default void postHandle(WebRequest request, ModelMap model) throws Exception{

    }

    @Override
    default void afterCompletion(WebRequest webRequest, Exception e) throws Exception {
        TenantContext.clear();
        clearTenant();
    }
}
