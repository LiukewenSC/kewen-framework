package com.kewen.framework.boot.auth.security.token;

import com.kewen.framework.boot.auth.security.model.SecurityUser;
import com.kewen.framework.boot.auth.token.TokenKeyGenerator;
import com.kewen.framework.boot.auth.token.TokenStore;
import com.kewen.framework.common.core.utils.UUIDUtil;
import org.springframework.beans.factory.annotation.Autowired;
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


    private TokenKeyGenerator keyGenerator;
    private TokenStore<Authentication> store;

    public Authentication getToken(HttpServletRequest httpServletRequest){

        String token = httpServletRequest.getHeader("Authorization");
        if (token ==null){
            return null;
        }
        token = token.substring("Bearer ".length());
        return store.get(token);
    }

    @Override
    public void onAuthentication(Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws SessionAuthenticationException {
        String token = keyGenerator.generateKey();
        SecurityUser user = (SecurityUser) authentication.getPrincipal();
        user.setToken(token);

        store.set(token,authentication);

    }

    public void setKeyGenerator(TokenKeyGenerator keyGenerator) {
        this.keyGenerator = keyGenerator;
    }

    public void setStore(TokenStore<Authentication> store) {
        this.store = store;
    }
}
