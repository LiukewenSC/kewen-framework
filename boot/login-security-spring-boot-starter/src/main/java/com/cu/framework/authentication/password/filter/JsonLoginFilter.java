package com.cu.framework.authentication.password.filter;


import com.alibaba.fastjson.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
/**
 * @author kewen
 * @descrpition JSON登录过滤器
 * @since 2024-07-02
 */
public class JsonLoginFilter extends UsernamePasswordAuthenticationFilter {
    /**
     * 认证尝试：仅支持 POST；Content-Type 为 JSON 时从请求体按配置的用户名/密码字段名解析出凭证，
     * 构造 UsernamePasswordAuthenticationToken 提交认证管理器；非 JSON 请求回退父类的表单参数方式
     * @throws AuthenticationServiceException 非 POST 请求或 JSON 请求体解析失败时抛出
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        if (!request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException(
                    "Authentication method not supported: " + request.getMethod());
        }
        String contentType = request.getContentType();
        if (contentType != null && contentType.toLowerCase().startsWith(MediaType.APPLICATION_JSON_VALUE)) {
            Map<String,String> map = null;
            try (ServletInputStream inputStream = request.getInputStream()){
                map = JSONObject.parseObject(inputStream, Map.class);
            } catch (IOException e) {
                throw new AuthenticationServiceException("解析登录JSON参数异常",e);
            }

            String username = map.get(getUsernameParameter());
            String password = map.get(getPasswordParameter());

            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
            setDetails(request, authenticationToken);
            return getAuthenticationManager().authenticate(authenticationToken);

        } else {
            return super.attemptAuthentication(request, response);
        }

    }
}
