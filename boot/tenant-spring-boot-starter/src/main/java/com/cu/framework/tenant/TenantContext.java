package com.cu.framework.tenant;

/**
 * @descrpition 租户上下文
 * @author kewen
 * @since 2022-12-05 9:44
 */
public class TenantContext {

    private static final ThreadLocal<Long> context = new InheritableThreadLocal<>();


    /**
     * 判断当前线程上下文中是否已设置租户 ID
     * @return 已设置租户 ID 返回 true，否则返回 false
     */
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
    public static void remove(){
        context.remove();
    }

}
