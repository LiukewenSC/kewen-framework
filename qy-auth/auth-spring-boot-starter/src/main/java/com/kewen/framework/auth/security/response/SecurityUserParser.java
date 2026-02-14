package com.kewen.framework.auth.security.response;

import com.kewen.framework.auth.security.model.SecurityUser;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;

public interface SecurityUserParser {
    boolean support(HttpServletRequest request, Authentication authentication);

    SecurityUser convert(HttpServletRequest request, Authentication authentication);
}
