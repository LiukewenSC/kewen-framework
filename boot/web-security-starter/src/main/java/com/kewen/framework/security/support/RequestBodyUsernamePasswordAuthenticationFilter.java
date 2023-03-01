package com.kewen.framework.security.support;

import com.alibaba.fastjson2.JSON;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * @descrpition 此类必须继承 UsernamePasswordAuthenticationFilter ，因为security提供的默认的处理器中包含了此顺序，只有在默认的处理器中的可以被替换至过滤器中。
 *  否则在http.apply(Configuration) 中config的时候会报错
 * @author kewen
 * @since 2023-02-28
 */
public class RequestBodyUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    public RequestBodyUsernamePasswordAuthenticationFilter() {
        super();

    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        if (request.getContentType().equals(MediaType.APPLICATION_JSON_VALUE) || request.getContentType().equals(MediaType.APPLICATION_JSON_UTF8_VALUE)) {
            Map<String,String> map = null;
            try (ServletInputStream inputStream = request.getInputStream()){
                map = JSON.parseObject(inputStream, Map.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

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
