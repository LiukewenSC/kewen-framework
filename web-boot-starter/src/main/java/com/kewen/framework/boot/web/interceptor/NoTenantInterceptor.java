package com.kewen.framework.boot.web.interceptor;

/**
 * @descrpition 没有租户拦截
 * @author kewen
 * @since 2022-12-05 10:11
 */
public class NoTenantInterceptor implements TenantInterceptor{
    @Override
    public void setTenant(long tenantId) {

    }

    @Override
    public void clearTenant() {

    }
}
