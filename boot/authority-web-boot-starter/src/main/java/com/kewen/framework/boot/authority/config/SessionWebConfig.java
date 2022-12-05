package com.kewen.framework.boot.authority.config;

import com.kewen.framework.boot.authority.currentuser.AbstractCurrentUserContextManager;
import com.kewen.framework.boot.authority.currentuser.session.SessionCurrentUserContextManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpSession;

/**
 * @descrpition 基于session实现的登录人存储
 * @author kewen
 * @since 2022-11-29 11:33
 */
@Configuration
@ConditionalOnProperty(name = "auth.store.type",havingValue = "session")
public class SessionWebConfig {

    @Autowired
    private HttpSession httpSession;
    /**
     * 基于session的用户上下文配置器
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(AbstractCurrentUserContextManager.class)
    public AbstractCurrentUserContextManager sessionCurrentUserContextInterceptor(){
        return new SessionCurrentUserContextManager(httpSession);
    }
}
