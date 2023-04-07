package com.kewen.framework.boot.auth.security;

import com.kewen.framework.base.common.model.IUser;
import com.kewen.framework.boot.auth.security.model.SecurityUser;
import com.kewen.framework.boot.common.context.UserContextContainer;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 基于springsecurity的用户上下文获取
 * @author kewen
 * @since 2023-04-07
 */
public class SecurityUserContextContainer implements UserContextContainer {


    @Override
    public IUser getUser() {
        return (SecurityUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}