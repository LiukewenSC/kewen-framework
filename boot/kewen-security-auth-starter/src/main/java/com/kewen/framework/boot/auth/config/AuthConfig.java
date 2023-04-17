package com.kewen.framework.boot.auth.config;

import com.kewen.framework.auth.context.AuthUserContext;
import com.kewen.framework.auth.core.AuthHandler;
import com.kewen.framework.auth.core.annotation.endpoint.AuthMenuInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author kewen
 * @descrpition
 * @since 2022-12-07 22:36
 */
@Configuration
public class AuthConfig {

    @Bean
    AuthUserContext authUserContext(){
        return new AuthUserContext();
    }

    @Bean
    public AuthMenuInterceptor authMenuInterceptor() {
        return new AuthMenuInterceptor();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(16);
    }


    /**
     * 在没有任何权限实现的时候默认使用系统权限实现，如果有自定义的权限实现则自定义优先
     * 1、创建 SysAuthHandler
     * 2、扫描包 com.kewen.framework.base.authority ，加入自定义的流程
     * 3、扫描包 com.kewen.framework.base.auth.bussiness.controller 加入默认的菜单相关的接口信息
     */
    @Configuration
    @ConditionalOnMissingBean(AuthHandler.class)
    @ComponentScan({
            "com.kewen.framework.auth.core",
            "com.kewen.framework.auth.sys"
            })
    public static class SysAuthConfig {

    }

}
