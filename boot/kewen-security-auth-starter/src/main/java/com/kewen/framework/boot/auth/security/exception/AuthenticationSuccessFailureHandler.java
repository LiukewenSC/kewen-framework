package com.kewen.framework.boot.auth.security.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kewen.framework.boot.auth.security.model.SecurityUser;
import com.kewen.framework.common.core.model.Result;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *  统一组装的认证成功+失败的控制器
 * @author kewen
 * @since 2023-04-18
 */
public class AuthenticationSuccessFailureHandler implements AuthenticationFailureHandler, AuthenticationSuccessHandler {


    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        Result result =Result.failed(exception.getMessage());
        writeResponseBody(response,result);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        SecurityUser principal = (SecurityUser)authentication.getPrincipal();
        Result result =Result.success(principal);
        writeResponseBody(response,result);
    }

    private void writeResponseBody(HttpServletResponse response,Result result) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        PrintWriter out = response.getWriter();
        out.write(new ObjectMapper().writeValueAsString(result));
        out.flush();
    }
}
