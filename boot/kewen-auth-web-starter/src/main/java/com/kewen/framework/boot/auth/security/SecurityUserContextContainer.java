package com.kewen.framework.boot.auth.security;

import com.kewen.framework.boot.auth.security.model.SecurityUser;
import com.kewen.framework.boot.auth.AuthUserInfo;
import com.kewen.framework.base.auth.context.AuthUserInfoContextContainer;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 基于springsecurity的用户上下文获取
 * @author kewen
 * @since 2023-04-07
 */
public class SecurityUserContextContainer implements AuthUserInfoContextContainer {

    @Override
    public AuthUserInfo getAuthUserInfo() {
        SecurityUser principal = (SecurityUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal.getAuthUserInfo();
    }
}