package com.kewen.framework.boot.auth.config;

import com.kewen.framework.base.authority.support.SysMenuAuthComposite;
import com.kewen.framework.base.authority.support.impl.MemorySysMenuAuthComposite;
import com.kewen.framework.boot.auth.annotation.endpoint.AuthMenuInterceptor;
import com.kewen.framework.boot.auth.web.currentuser.AbstractCurrentUserContextManager;
import com.kewen.framework.boot.auth.web.currentuser.session.SessionCurrentUserContextManager;
import com.kewen.framework.boot.auth.web.currentuser.token.DefaultTokenKeyGenerator;
import com.kewen.framework.boot.auth.web.currentuser.token.MemoryTokenUserDetailSore;
import com.kewen.framework.boot.auth.web.currentuser.token.TokenCurrentUserContextManager;
import com.kewen.framework.boot.auth.web.currentuser.token.TokenKeyGenerator;
import com.kewen.framework.boot.auth.web.currentuser.token.TokenUserDetailStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpSession;

/**
 * @author kewen
 * @descrpition
 * @since 2022-11-23 9:20
 */
@Configuration
public class AuthWebConfig implements WebMvcConfigurer {

    @Bean
    public SysMenuAuthComposite sysMenuAuthComposite(){
        return new MemorySysMenuAuthComposite();
    }

    @Autowired
    private AuthMenuInterceptor authMenuInterceptor;
    @Autowired
    AbstractCurrentUserContextManager currentUserContextInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        //用户当前用户拦截器，此拦截器一定要在最前面，保证当前用户设置到上下文中
        registry.addInterceptor(currentUserContextInterceptor).excludePathPatterns("/login/**");

        //此拦截器放在当前用户拦截器之后，里面内容会先从用户上下文中获取数据
        registry.addInterceptor(authMenuInterceptor);

    }


    /**
     * @descrpition 基于session实现的登录人存储
     * @author kewen
     * @since 2022-11-29 11:33
     */
    @Configuration
    @ConditionalOnProperty(name = "auth.store.type",havingValue = "session")
    public static class SessionWebConfig {

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

    /**
     * @descrpition  基于token实现的登录人存储
     * @author kewen
     * @since 2022-11-29 11:35
     */
    @Configuration
    @ConditionalOnProperty(name = "auth.store.type",havingValue = "token")
    public static class TokenWebConfig {

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
}
