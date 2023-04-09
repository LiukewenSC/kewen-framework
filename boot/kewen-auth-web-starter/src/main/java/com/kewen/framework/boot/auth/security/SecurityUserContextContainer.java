package com.kewen.framework.boot.auth.security;

import com.kewen.framework.base.authority.model.UserDetail;
import com.kewen.framework.base.common.model.IUser;
import com.kewen.framework.boot.auth.context.UserDetailContextContainer;
import com.kewen.framework.boot.auth.security.model.SecurityUser;
import com.kewen.framework.boot.common.context.UserContextContainer;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 基于springsecurity的用户上下文获取
 * @author kewen
 * @since 2023-04-07
 */
public class SecurityUserContextContainer implements UserContextContainer, UserDetailContextContainer {


    @Override
    public IUser getUser() {
        return (SecurityUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Override
    public UserDetail getUserDetail() {
        SecurityUser principal = (SecurityUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal.getUserDetail();
    }
}