package com.kewen.framework.boot.auth.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.session.SessionManagementFilter;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * 
 * @author kewen
 * @since 2023-04-17
 */
@Slf4j
public class TokenFilter  extends SessionManagementFilter {

    public TokenFilter(SecurityContextRepository securityContextRepository) {
        super(securityContextRepository);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        log.info("替换了sessionManager，准备修改");
    }
}