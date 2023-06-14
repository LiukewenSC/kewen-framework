package com.kewen.framework.boot.auth.security;

import com.kewen.framework.auth.core.model.AuthUserInfo;
import com.kewen.framework.boot.auth.security.model.SecurityUser;
import com.kewen.framework.auth.context.AuthUserInfoContextContainer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * 基于springsecurity的用户上下文获取
 * @author kewen
 * @since 2023-04-07
 */
public class SecurityUserContextContainer implements AuthUserInfoContextContainer {

    @Override
    public AuthUserInfo getAuthUserInfo() {
        return Optional.of(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal)
                .filter(p->p instanceof SecurityUser)
                .map(p -> (SecurityUser) p)
                .map(SecurityUser::getAuthUserInfo)
                .orElse(null);
    }
}