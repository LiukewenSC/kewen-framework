package com.kewen.framework.boot.auth;

import com.kewen.framework.auth.core.annotation.endpoint.AuthMenuInterceptor;
import com.kewen.framework.auth.core.model.AuthUserInfo;
import com.kewen.framework.boot.auth.token.MemoryTokenStore;
import com.kewen.framework.boot.auth.token.TokenStore;
import com.kewen.framework.boot.auth.web.PermitUrlInterceptor;
import com.kewen.framework.boot.auth.web.WebUserContextContainer;
import com.kewen.framework.boot.auth.web.session.WebSessionUserContextContainer;
import com.kewen.framework.boot.auth.token.DefaultTokenKeyGenerator;
import com.kewen.framework.boot.auth.web.token.WebTokenUserContextContainer;
import com.kewen.framework.boot.auth.token.TokenKeyGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpSession;

/**
 * @author kewen
 * @descrpition
 * @since 2022-11-23 9:20
 */
@Configuration
@ConditionalOnMissingClass("org.springframework.security.config.annotation.web.configuration.EnableWebSecurity")
@Import({WebAuthConfig.SessionWebConfig.class, WebAuthConfig.TokenWebConfig.class})
@ComponentScan("com.kewen.framework.boot.auth.web")
@Slf4j
public class WebAuthConfig implements WebMvcConfigurer {

    @Autowired
    private AuthMenuInterceptor authMenuInterceptor;
    @Autowired
    AuthProperties authProperties;
    @Autowired
    PermitUrlContainer permitUrlContainer;

    @Bean
    public PermitUrlInterceptor permitUrlInterceptor(){
        PermitUrlInterceptor interceptor = new PermitUrlInterceptor();
        interceptor.setPermitUrlContainer(permitUrlContainer);
        interceptor.setAuthProperties(authProperties);
        return interceptor;
    }

    public WebAuthConfig() {
        log.info("使用SpringWeb作为安全框架");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(permitUrlInterceptor());
        //此拦截器放在当前用户拦截器之后，里面内容会先从用户上下文中获取数据
        registry.addInterceptor(authMenuInterceptor);

    }


    /**
     * @descrpition 基于session实现的登录人存储
     * @author kewen
     * @since 2022-11-29 11:33
     */
    @Configuration
    @ConditionalOnProperty(name = "kewen.auth.store.type",havingValue = "session")
    public static class SessionWebConfig {

        @Autowired
        private HttpSession httpSession;
        /**
         * 基于session的用户上下文配置器
         * @return
         */
        @Bean
        @ConditionalOnMissingBean(WebUserContextContainer.class)
        public WebUserContextContainer sessionCurrentUserContextInterceptor(){
            return new WebSessionUserContextContainer(httpSession);
        }
    }

    /**
     * @descrpition  基于token实现的登录人存储
     * @author kewen
     * @since 2022-11-29 11:35
     */
    @Configuration
    @ConditionalOnProperty(name = "kewen.auth.store.type",havingValue = "token")
    public static class TokenWebConfig {

        @Autowired
        AuthProperties authProperties;


        /**
         * 默认的基于token的用户上下文配置器
         * @return
         */
        @Bean
        @ConditionalOnMissingBean(WebTokenUserContextContainer.class)
        public WebTokenUserContextContainer tokenCurrentUser(){
            return new WebTokenUserContextContainer();
        }

        @Bean
        @ConditionalOnMissingBean(TokenKeyGenerator.class)
        public TokenKeyGenerator tokenKeyGenerator(){
            return new DefaultTokenKeyGenerator();
        }

        @Bean
        public TokenStore<AuthUserInfo> tokenUserDetailStore(){
            return new MemoryTokenStore<AuthUserInfo>(authProperties.getStore().getExpireTime());
        }
    }
}
