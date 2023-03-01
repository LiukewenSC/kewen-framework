package com.kewen.framework.security;

import com.alibaba.fastjson2.JSON;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * @author kewen
 * @descrpition
 * @since 2023-02-28
 */
public class RequestBodyUsernamePasswordAuthenticationProcessingFilter extends AbstractAuthenticationProcessingFilter {

    public RequestBodyUsernamePasswordAuthenticationProcessingFilter() {
        super(new AntPathRequestMatcher("/login", "POST"));
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {

        if (request.getContentType().equals(MediaType.APPLICATION_JSON_VALUE) || request.getContentType().equals(MediaType.APPLICATION_JSON_UTF8_VALUE)) {

            ServletInputStream inputStream = request.getInputStream();

            Map<String,String> map = JSON.parseObject(inputStream, Map.class);

            String username = map.get("username");
            String password = map.get("password");

            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
            Authentication authenticate = getAuthenticationManager().authenticate(authenticationToken);

            return authenticate;

        } else {
            throw new RuntimeException();
        }


    }
}
