package com.cu.framework.auth.core.data.range;


/**
 * @descrpition 权限范围上下文，用于
 * @author kewen
 * @since 2022-11-23
 */
public class DataRangeContext {

    /** 当前线程的数据权限范围，可被子线程继承 */
    private static ThreadLocal<AuthRange> local = new InheritableThreadLocal<>();

    /**
     * 获取当前线程上下文中的数据权限范围
     * @return 数据权限范围，未设置时返回 null
     */
    public static AuthRange get(){
        return local.get();
    }
    /**
     * 将数据权限范围写入当前线程上下文
     * @param authRange 数据权限范围
     */
    public static void set(AuthRange authRange){
        local.set(authRange);
    }
    /**
     * 清除当前线程上下文中的数据权限范围
     */
    public static void clear(){
        local.remove();
    }

}
