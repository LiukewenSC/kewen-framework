package com.kewen.framework.base.common.context;

import com.kewen.framework.base.common.model.User;

/**
 * @descrpition 用户上下文
 * @author kewen
 * @since 2022-12-05 16:24
 */
public class UserContext {
    private static final ThreadLocal<User> context = new ThreadLocal<>();


    /**
     * 获取租户id
     * @return
     */
    public static User get(){
        return context.get();
    }

    /**
     * 设置当前人
     * @param user
     */
    public static void set(User user){
        context.set(user);
    }

    /**
     * 清除
     */
    public static void clear(){
        context.remove();
    }
}
