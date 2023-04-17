package com.kewen.framework.boot.auth.config;

import com.kewen.framework.auth.core.annotation.endpoint.AuthMenuInterceptor;
import com.kewen.framework.boot.auth.web.WebAuthUserInfoContextContainer;
import com.kewen.framework.boot.auth.web.session.SessionCurrentUserInfoContextContainer;
import com.kewen.framework.boot.auth.web.token.DefaultTokenKeyGenerator;
import com.kewen.framework.boot.auth.web.token.MemoryTokenUserDetailSore;
import com.kewen.framework.boot.auth.web.token.TokenCurrentUserInfoContextContainer;
import com.kewen.framework.boot.auth.web.token.TokenKeyGenerator;
import com.kewen.framework.boot.auth.web.token.TokenUserDetailStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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



    @Value("kewen.auth.login-endpoint")
    private String loginEndpoint;

    @Autowired
    private AuthMenuInterceptor authMenuInterceptor;


    public WebAuthConfig() {

        log.info("使用SpringWeb作为安全框架");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        //此拦截器放在当前用户拦截器之后，里面内容会先从用户上下文中获取数据
        registry.addInterceptor(authMenuInterceptor);

    }


    /**
     * @descrpition 基于session实现的登录人存储
     * @author kewen
     * @since 2022-11-29 11:33
     */
    @Configuration
    @ConditionalOnProperty(name = "kewen.auth.web.store.type",havingValue = "session")
    public static class SessionWebConfig {

        @Autowired
        private HttpSession httpSession;
        /**
         * 基于session的用户上下文配置器
         * @return
         */
        @Bean
        @ConditionalOnMissingBean(WebAuthUserInfoContextContainer.class)
        public WebAuthUserInfoContextContainer sessionCurrentUserContextInterceptor(){
            return new SessionCurrentUserInfoContextContainer(httpSession);
        }
    }

    /**
     * @descrpition  基于token实现的登录人存储
     * @author kewen
     * @since 2022-11-29 11:35
     */
    @Configuration
    @ConditionalOnProperty(name = "kewen.auth.web.store.type",havingValue = "token")
    public static class TokenWebConfig {

        /**
         * 默认的基于token的用户上下文配置器
         * @return
         */
        @Bean
        @ConditionalOnMissingBean(TokenCurrentUserInfoContextContainer.class)
        public TokenCurrentUserInfoContextContainer tokenCurrentUser(){
            return new TokenCurrentUserInfoContextContainer();
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
