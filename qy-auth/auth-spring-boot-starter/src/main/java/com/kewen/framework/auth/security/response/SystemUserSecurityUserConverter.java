package com.kewen.framework.auth.security.response;

import com.kewen.framework.auth.security.model.SecurityUser;

import javax.servlet.http.HttpServletRequest;

/**
 * 2026/02/14
 *
 * @author kewen
 * @since 4.24.0-SP01
 */
public class SystemUserSecurityUserConverter implements SecurityUserConverter {
    @Override
    public boolean support(HttpServletRequest request, Object principal) {
        return principal instanceof SecurityUser;
    }

    @Override
    public SecurityUser convert(HttpServletRequest request, Object principal) {
        return (SecurityUser) principal;
    }
}
