package com.kewen.framework.auth.security.response;

import com.kewen.framework.auth.security.model.SecurityUser;

import javax.servlet.http.HttpServletRequest;

public interface SecurityUserConverter {
    boolean support(HttpServletRequest request, Object principal);

    SecurityUser convert(HttpServletRequest request, Object principal);
}
