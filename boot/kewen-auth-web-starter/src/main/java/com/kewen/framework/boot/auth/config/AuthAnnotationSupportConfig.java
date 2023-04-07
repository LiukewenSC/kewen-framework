package com.kewen.framework.boot.auth.config;

import com.kewen.framework.base.authority.support.SysMenuAuthComposite;
import com.kewen.framework.base.authority.support.impl.MemorySysMenuAuthComposite;
import com.kewen.framework.boot.auth.annotation.endpoint.AuthMenuInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author kewen
 * @descrpition
 * @since 2022-12-07 22:36
 */
@Configuration
@ComponentScan("com.kewen.framework.base.authority")
public class AuthAnnotationSupportConfig {

    @Bean
    public AuthMenuInterceptor authMenuInterceptor(SysMenuAuthComposite menuService){
        return new AuthMenuInterceptor(menuService);
    }


}
