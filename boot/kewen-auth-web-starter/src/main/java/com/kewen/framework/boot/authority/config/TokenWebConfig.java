package com.kewen.framework.boot.authority.config;

import com.kewen.framework.boot.authority.currentuser.token.DefaultTokenKeyGenerator;
import com.kewen.framework.boot.authority.currentuser.token.MemoryTokenUserDetailSore;
import com.kewen.framework.boot.authority.currentuser.token.TokenCurrentUserContextManager;
import com.kewen.framework.boot.authority.currentuser.token.TokenKeyGenerator;
import com.kewen.framework.boot.authority.currentuser.token.TokenUserDetailStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @descrpition  基于token实现的登录人存储
 * @author kewen
 * @since 2022-11-29 11:35
 */
@Configuration
@ConditionalOnProperty(name = "auth.store.type",havingValue = "token")
public class TokenWebConfig {

    /**
     * 默认的基于token的用户上下文配置器
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(TokenCurrentUserContextManager.class)
    public TokenCurrentUserContextManager tokenCurrentUser(){
        return new TokenCurrentUserContextManager(tokenUserDetailStore(),tokenKeyGenerator());
    }
    @Bean
    @ConditionalOnMissingBean(TokenKeyGenerator.class)
    public TokenKeyGenerator tokenKeyGenerator(){
        return new DefaultTokenKeyGenerator();
    }
    @Bean
    public TokenUserDetailStore tokenUserDetailStore(){
        return new MemoryTokenUserDetailSore();
    }
}
