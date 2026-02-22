package com.kewen.framework.auth.security.response;

import com.kewen.framework.auth.security.model.SecurityUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * 2026/02/22
 *
 * @author kewen
 * @since 1.0
 */
public class SecurityUserAuthenticationSuccessResultConverter implements AuthenticationSuccessResultConverter {
    @Override
    public boolean support(Authentication authentication) {
        return authentication.getPrincipal() instanceof SecurityUser;
    }

    @Override
    public JsonSuccessResult convert(Authentication authentication) {
        SecurityUser securityUser = (SecurityUser)authentication.getPrincipal();
        return convert(securityUser);
    }

    @Override
    public SecurityUser convert(UserDetailsService userDetailsService, Authentication authentication) {
        SecurityUser securityUser = (SecurityUser)authentication.getPrincipal();
        return securityUser;
    }
}
