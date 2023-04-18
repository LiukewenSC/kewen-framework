package com.kewen.framework.boot.auth.security.token;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.SecurityContextRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *  Token对应的安全上下文 Repository
 *  模拟 HttpSessionSecurityContextRepository
 * {@link  org.springframework.security.web.context.HttpSessionSecurityContextRepository}
 * @author kewen
 * @since 2023-04-18
 */
public class TokenSecurityContextRepository implements SecurityContextRepository {
    @Override
    public SecurityContext loadContext(HttpRequestResponseHolder requestResponseHolder) {
        return SecurityContextHolder.createEmptyContext();
    }

    @Override
    public void saveContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {
        //原来基于session的会将上下文保存在 session 中 ，基于token的不需要
    }

    @Override
    public boolean containsContext(HttpServletRequest request) {
        return false;
    }
}
