package com.cu.framework.auth.security.response;

import com.cu.framework.auth.security.model.SecurityUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * 2026/02/22
 *
 * @author kewen
 * @since 1.0
 */
public class SecurityUserAuthenticationSuccessResultConverter implements AuthenticationSuccessResultConverter {
    /**
     * 认证主体为 SecurityUser 时支持转换
     */
    @Override
    public boolean support(Authentication authentication) {
        return authentication.getPrincipal() instanceof SecurityUser;
    }

    /**
     * 取出认证主体 SecurityUser，按接口默认实现逐字段拷贝为 JsonSuccessResult
     */
    @Override
    public JsonSuccessResult convert(Authentication authentication) {
        SecurityUser securityUser = (SecurityUser)authentication.getPrincipal();
        return convert(securityUser);
    }

    /**
     * 直接返回认证主体中的 SecurityUser，不经过 userDetailsService 重新加载
     */
    @Override
    public SecurityUser convert(UserDetailsService userDetailsService, Authentication authentication) {
        SecurityUser securityUser = (SecurityUser)authentication.getPrincipal();
        return securityUser;
    }
}
