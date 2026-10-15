package com.cu.framework.auth.core;


import com.cu.framework.auth.core.entity.CurrentUser;
import com.cu.framework.auth.core.entity.IAuthObject;
import com.cu.framework.auth.core.entity.BaseAuth;

import java.util.Collection;
import java.util.Collections;

/**
 * 具有权限的用户上下文，用户登录了之后每次请求都应该有上下文信息
 * 实现类通过 SecurityUserContext（login-spring-boot-starter）获取当前登录用户
 */
public interface AuthCurrentUserContextHolder {

    CurrentUser getUser();

    /**
     * 获取当前登录用户的基础权限集合
     * @return 权限集合，上下文中无用户或无权限对象时返回空集合
     */
    default Collection<BaseAuth> getAuths(){
        if (getUser() == null || getAuthObject() == null) {
            return Collections.emptyList();
        }
        return getAuthObject().listBaseAuth();
    }
    /**
     * 获取当前登录用户的基础权限集合
     * @return 权限集合，上下文中无用户或无权限对象时返回空集合
     */
    IAuthObject getAuthObject();

}
