package com.kewen.framework.boot.auth;

import com.kewen.framework.auth.context.AuthUserContext;
import com.kewen.framework.auth.core.AuthHandler;
import com.kewen.framework.auth.core.annotation.endpoint.AuthMenuInterceptor;
import com.kewen.framework.auth.sys.support.SysAuthHandler;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @author kewen
 * @descrpition
 * @since 2022-12-07 22:36
 */
@Configuration
public class AuthConfig {

    @Bean
    PermitUrlContainer permitUrlContainer(){
        return new PermitUrlContainer();
    }

    @Bean
    AuthUserContext authUserContext(){
        return new AuthUserContext();
    }

    @Bean
    public AuthMenuInterceptor authMenuInterceptor() {
        return new AuthMenuInterceptor();
    }

    @Bean
    public AuthPasswordEncodeWrapper passwordEncoder() {
        return new AuthPasswordEncodeWrapper(new BCryptPasswordEncoder());
    }


    /**
     * 在没有任何权限实现的时候默认使用系统权限实现，如果有自定义的权限实现则自定义优先
     * 1、创建 SysAuthHandler
     * 2、扫描包 com.kewen.framework.auth...加入自定义的流程
     * 3、扫描包 com.kewen.framework.auth.bussiness.controller 加入默认的菜单相关的接口信息
     */
    @Configuration
    @ConditionalOnMissingBean(AuthHandler.class)
    @ComponentScan({
            "com.kewen.framework.auth.core",
            "com.kewen.framework.auth.sys"
            })
    @MapperScan({
            "com.kewen.framework.auth.sys.composite.mapper",
            "com.kewen.framework.auth.sys.mp.mapper"
    })
    public static class SysAuthConfig {

        @Bean
        public SysAuthHandler sysAuthHandler(){
            return new SysAuthHandler();
        }

    }

}
