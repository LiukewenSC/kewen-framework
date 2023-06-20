package com.kewen.framework.boot.auth.security;

import com.kewen.framework.boot.auth.PermitUrlContainer;
import com.kewen.framework.boot.auth.security.token.TokenAuthenticationStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

/**
 * 
 * @author kewen
 * @since 2023-04-17
 */
@Slf4j
public class TokenFilter  extends GenericFilterBean {

    private TokenAuthenticationStrategy tokenAuthenticationStrategy;
    private AuthenticationFailureHandler failureHandler;
    private PermitUrlContainer permitUrlContainer;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        doFilter((HttpServletRequest)servletRequest, (HttpServletResponse)servletResponse, filterChain);
    }
    public void doFilter(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws IOException, ServletException {
        String uri = httpServletRequest.getRequestURI();

        if (!isMatches(uri)){
            Authentication token = tokenAuthenticationStrategy.getToken(httpServletRequest);
            if (token ==null){
                failureHandler.onAuthenticationFailure(httpServletRequest,httpServletResponse,new SessionAuthenticationException("尚未登录或登录已过期，请重新登录"));
                return;
            } else {
                SecurityContextHolder.getContext().setAuthentication(token);
            }
        }
        filterChain.doFilter(httpServletRequest,httpServletResponse);

    }

    private boolean isMatches(String uri){
        String[] permitUrls = permitUrlContainer.getPermitUrls();

        for (String permitUrl : permitUrls) {
            if (pathMatcher.match(permitUrl,uri)) {
                return true;
            }
        }
        return false;
    }


    public void setTokenAuthenticationStrategy(TokenAuthenticationStrategy tokenAuthenticationStrategy) {
        this.tokenAuthenticationStrategy = tokenAuthenticationStrategy;
    }

    public void setFailureHandler(AuthenticationFailureHandler failureHandler) {
        this.failureHandler = failureHandler;
    }

    public void setPermitUrlContainer(PermitUrlContainer permitUrlContainer) {
        this.permitUrlContainer = permitUrlContainer;
    }
}