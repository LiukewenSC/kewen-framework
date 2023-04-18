package com.kewen.framework.boot.auth.security;

import com.kewen.framework.boot.auth.security.token.TokenAuthenticationStrategy;
import com.kewen.framework.boot.auth.security.token.TokenSecurityContextRepository;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.session.SessionManagementFilter;

/**
 * 
 * @author kewen
 * @since 2023-04-18
 */
public class TokenManagementConfigurer<H extends HttpSecurityBuilder<H>> extends AbstractHttpConfigurer<SessionManagementConfigurer<H>, H> {

    TokenAuthenticationStrategy sessionAuthenticationStrategy;
    AuthenticationFailureHandler authenticationFailureHandler;
    TokenFilter tokenFilter ;
    public TokenManagementConfigurer() {
        this.tokenFilter = new TokenFilter();
    }

    public TokenManagementConfigurer<H> sessionAuthenticationStrategy(){
        sessionAuthenticationStrategy = new TokenAuthenticationStrategy();
        //移除掉session的，因为不需要session了
        getBuilder().removeConfigurer(SessionManagementConfigurer.class);

        //需要修改SecurityContextRepository，否则SessionManagementConfigurer 默认将 HttpSessionSecurityContextRepository 加入到SharedObject中
        getBuilder().setSharedObject(SecurityContextRepository.class,new TokenSecurityContextRepository());

        return this;
    }
    public TokenManagementConfigurer<H> authenticationFailureHandler(AuthenticationFailureHandler failureHandler){
        this.authenticationFailureHandler=failureHandler;
        return this;
    }

    @Override
    public void init(H http) throws Exception {

        http.setSharedObject(SessionAuthenticationStrategy.class,this.sessionAuthenticationStrategy);


    }

    @Override
    public void configure(H http) throws Exception {

        tokenFilter.setTokenAuthenticationStrategy(sessionAuthenticationStrategy);
        tokenFilter.setFailureHandler(authenticationFailureHandler);
        tokenFilter = postProcess(tokenFilter);
        tokenFilter.setFailureHandler(authenticationFailureHandler);
        http.addFilterAfter(tokenFilter, SessionManagementFilter.class);

    }
}
