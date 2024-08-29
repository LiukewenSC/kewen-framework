package com.kewen.framework.common.tenant;

/**
 * @descrpition 租户上下文
 * @author kewen
 * @since 2022-12-05 9:44
 */
public class TenantContext {

    private static final ThreadLocal<String> context = new InheritableThreadLocal<>();


    public static boolean support(){
        return context.get() !=null;
    }
    /**
     * 获取租户id
     * @return
     */
    public static String get(){
        return context.get();
    }

    /**
     * 设置租户
     * @param tenantId
     */
    public static void set(String tenantId){
        context.set(tenantId);
    }

    /**
     * 清除
     */
    public static void clear(){
        context.remove();
    }

}
