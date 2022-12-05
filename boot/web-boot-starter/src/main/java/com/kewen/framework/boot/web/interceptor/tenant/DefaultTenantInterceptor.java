package com.kewen.framework.boot.web.interceptor.tenant;

import com.kewen.common.context.TenantContext;

/**
 * @author kewen
 * @descrpition 租户拦截器 ，配置租户上下文
 * @since 2022-12-05 9:42
 */
public class DefaultTenantInterceptor implements TenantInterceptor {


    @Override
    public void setTenant(long tenantId) {
        TenantContext.set(tenantId);
    }

    @Override
    public void clearTenant() {
        TenantContext.clear();
    }
}
