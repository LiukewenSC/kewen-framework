package com.kewen.framework.tenant;

/**
 * @descrpition 租户上下文
 * @author kewen
 * @since 2022-12-05 9:44
 */
public class TenantContext {

    private static final ThreadLocal<Long> context = new InheritableThreadLocal<>();


    public static boolean support(){
        return context.get() !=null;
    }
    /**
     * 获取租户id
     * @return
     */
    public static Long get(){
        return context.get();
    }

    /**
     * 设置租户
     * @param tenantId
     */
    public static void set(Long tenantId){
        context.set(tenantId);
    }

    /**
     * 清除
     */
    public static void clear(){
        context.remove();
    }

}
