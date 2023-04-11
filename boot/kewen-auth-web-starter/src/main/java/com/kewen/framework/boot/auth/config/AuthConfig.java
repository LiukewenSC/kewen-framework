package com.kewen.framework.boot.auth.config;

import com.kewen.framework.boot.auth.annotation.endpoint.AuthMenuInterceptor;
import com.kewen.framework.boot.auth.AuthHandler;
import com.kewen.framework.boot.auth.sys.SysAuthHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author kewen
 * @descrpition
 * @since 2022-12-07 22:36
 */
@Configuration
@PropertySource("classpath:application-auth-web.properties")
@ComponentScan("com.kewen.framework.boot.auth.bussiness")
public class AuthConfig {

    @Bean
    public AuthMenuInterceptor authMenuInterceptor() {
        return new AuthMenuInterceptor();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }


    /**
     * 在没有任何权限实现的时候默认使用系统权限实现，如果有自定义的权限实现则自定义优先
     * 1、创建 SysAuthHandler
     * 2、扫描包 com.kewen.framework.base.authority ，加入自定义的流程
     * 3、扫描包 com.kewen.framework.boot.auth.bussiness.controller 加入默认的菜单相关的接口信息
     */
    @Configuration
    @ConditionalOnMissingBean(AuthHandler.class)
    @ComponentScan({"com.kewen.framework.base.authority","com.kewen.framework.boot.auth.bussiness.controller"})
    public static class SysAuthConfig {
        @Bean
        public SysAuthHandler sysAuthHandler() {
            return new SysAuthHandler();
        }
    }

}
