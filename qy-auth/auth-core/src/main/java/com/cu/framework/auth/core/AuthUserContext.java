package com.cu.framework.auth.core;


import com.cu.framework.auth.core.entity.CurrentUser;
import com.cu.framework.auth.core.entity.IAuthObject;
import com.cu.framework.auth.core.entity.BaseAuth;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * 具有权限的用户上下文，用户登录了之后每次请求都应该有上下文信息
 * 记得使用前应该先登录，然后存储到CurrentAuthUserContextContainer中
 */
public class AuthUserContext {
    /** 当前登录用户的线程变量，可被子线程继承 */
    private static final ThreadLocal<CurrentUser> CURRENT_USER_LOCAL = new InheritableThreadLocal<>();
    /**
     * 获取当前登录用户的基础权限集合
     * @return 权限集合，上下文中无用户或无权限对象时返回空集合
     */
    public static Collection<BaseAuth> getAuths() {
        return Optional.ofNullable(getCurrentUser())
                .map(CurrentUser::getAuthObject)
                .map(IAuthObject::listBaseAuth)
                .orElse(Collections.emptyList());
    }
    /**
     * 将当前登录用户绑定到当前线程，登录成功后调用
     * @param currentUser 当前登录用户
     */
    public static void setCurrentUser(CurrentUser currentUser) {
        CURRENT_USER_LOCAL.set(currentUser);
    }
    /**
     * 获取当前线程绑定的登录用户
     * @return 当前登录用户，未设置时返回 null
     */
    public static CurrentUser getCurrentUser() {
        return CURRENT_USER_LOCAL.get();
    }

}
