package com.kewen.framework.boot.auth.security.token;

import com.kewen.framework.auth.sys.mp.entity.SysUser;
import com.kewen.framework.boot.auth.security.model.SecurityUser;
import com.kewen.framework.common.core.utils.UUIDUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * @author kewen
 * @since 2023-04-18
 */
public class TokenAuthenticationStrategy implements SessionAuthenticationStrategy {


    ConcurrentHashMap<String, Authentication> tokenCache = new ConcurrentHashMap<>();


    public Authentication getToken(HttpServletRequest httpServletRequest){
        String token = httpServletRequest.getHeader("token");
        if (token ==null){
            return null;
        }
        return tokenCache.get(token);
    }

    @Override
    public void onAuthentication(Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws SessionAuthenticationException {

        String token = UUIDUtil.generate();

        SecurityUser user = (SecurityUser) authentication.getPrincipal();
        user.setToken(token);

        tokenCache.put(token,authentication);

    }
}
