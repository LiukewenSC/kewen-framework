package com.kewen.framework.datasource.context;


import com.kewen.framework.common.core.context.TenantContext;

/**
 * 默认租户配置器
 * @author liukewen
 * @since 2022/9/1
 */
public class DefaultDbTenant implements DbTenant {

    private static Long get(){
        return TenantContext.get();
    }

    /**
     * 设置租户
     * @param tenant
     */
    public static void setTenant(Integer tenant){
        TenantContext.set(tenant);
    }
    /**
     * 移除租户
     *
     */
    public static void clear(){
        TenantContext.clear();
    }

    @Override
    public Long getTenantId() {
        return get();
    }
}