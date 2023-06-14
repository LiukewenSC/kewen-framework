package com.kewen.framework.boot.auth.security;

import com.kewen.framework.boot.auth.PermitUrlContainer;
import com.kewen.framework.boot.auth.security.token.TokenAuthenticationStrategy;
import com.kewen.framework.boot.auth.security.token.TokenSecurityContextRepository;
import com.kewen.framework.boot.auth.token.DefaultTokenKeyGenerator;
import com.kewen.framework.boot.auth.token.TokenKeyGenerator;
import com.kewen.framework.boot.auth.token.TokenStore;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.session.SessionManagementFilter;

/**
 *  基于token的登录信息存储配置，本配置移除了session相关的东西
 * @author kewen
 * @since 2023-04-18
 */
public class TokenManagementConfigurer<H extends HttpSecurityBuilder<H>> extends AbstractHttpConfigurer<SessionManagementConfigurer<H>, H> {

    TokenAuthenticationStrategy tokenAuthenticationStrategy = new TokenAuthenticationStrategy();
    TokenStore<Authentication> tokenStore ;
    TokenKeyGenerator tokenKeyGenerator = new DefaultTokenKeyGenerator();
    AuthenticationFailureHandler authenticationFailureHandler;
    PermitUrlContainer permitUrlContainer;
    TokenFilter tokenFilter;
    private boolean isRemoveSession = false;

    public TokenManagementConfigurer() {
        this.tokenFilter = new TokenFilter();
    }

    public TokenManagementConfigurer<H> tokenAuthenticationStrategy(TokenAuthenticationStrategy tokenAuthenticationStrategy) {
        this.tokenAuthenticationStrategy=tokenAuthenticationStrategy;
        return this;
    }

    public TokenManagementConfigurer<H> removeSessionConfig() {
        //移除掉session的，因为不需要session了
        getBuilder().removeConfigurer(SessionManagementConfigurer.class);
        //需要修改SecurityContextRepository，否则SessionManagementConfigurer 默认将 HttpSessionSecurityContextRepository 加入到SharedObject中
        getBuilder().setSharedObject(SecurityContextRepository.class, new TokenSecurityContextRepository());
        isRemoveSession = true;
        return this;
    }

    public TokenManagementConfigurer<H> tokenStore(TokenStore<Authentication> tokenStore) {
        this.tokenStore = tokenStore;
        return this;
    }

    public TokenManagementConfigurer<H> keyGenerator(TokenKeyGenerator tokenKeyGenerator) {
        this.tokenKeyGenerator = tokenKeyGenerator;
        return this;
    }

    public TokenManagementConfigurer<H> authenticationFailureHandler(AuthenticationFailureHandler failureHandler) {
        this.authenticationFailureHandler = failureHandler;
        return this;
    }
    public TokenManagementConfigurer<H> permitUrlContainer(PermitUrlContainer permitUrlContainer) {
        this.permitUrlContainer = permitUrlContainer;
        return this;
    }

    @Override
    public void init(H http) throws Exception {

        if (!isRemoveSession) {
            removeSessionConfig();
        }
        http.setSharedObject(SessionAuthenticationStrategy.class, this.tokenAuthenticationStrategy);

    }

    @Override
    public void configure(H http) throws Exception {
        tokenAuthenticationStrategy.setStore(tokenStore);
        tokenAuthenticationStrategy.setKeyGenerator(tokenKeyGenerator);
        tokenFilter.setTokenAuthenticationStrategy(tokenAuthenticationStrategy);
        tokenFilter.setPermitUrlContainer(permitUrlContainer);
        tokenFilter.setFailureHandler(authenticationFailureHandler);
        tokenFilter = postProcess(tokenFilter);
        tokenFilter.setFailureHandler(authenticationFailureHandler);
        http.addFilterAfter(tokenFilter, SessionManagementFilter.class);

    }
}
