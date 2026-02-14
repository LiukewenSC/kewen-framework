package com.kewen.framework.auth.security.response;

import com.kewen.framework.auth.security.model.SecurityUser;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;

/**
 * 2026/02/14
 *
 * @author kewen
 * @since 4.24.0-SP01
 */
public class SystemUserSecurityUserParser implements SecurityUserParser {
    @Override
    public boolean support(HttpServletRequest request, Authentication authentication) {
        return authentication.getPrincipal() instanceof SecurityUser;
    }

    @Override
    public SecurityUser convert(HttpServletRequest request, Authentication authentication) {
        return (SecurityUser) authentication.getPrincipal();
    }
}
