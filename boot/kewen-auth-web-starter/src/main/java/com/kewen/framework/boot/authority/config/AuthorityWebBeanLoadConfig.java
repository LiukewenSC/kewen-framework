package com.kewen.framework.boot.authority.config;

import com.kewen.framework.base.authority.support.SysMenuAuthComposite;
import com.kewen.framework.base.authority.support.impl.MemorySysMenuAuthComposite;
import com.kewen.framework.boot.authority.advance.menucheck.AuthMenuInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author kewen
 * @descrpition
 * @since 2022-12-07 22:36
 */
@Configuration
public class AuthorityWebBeanLoadConfig {

    @Bean
    public AuthMenuInterceptor authMenuInterceptor(SysMenuAuthComposite menuService){
        return new AuthMenuInterceptor(menuService);
    }

    @Bean
    public SysMenuAuthComposite sysMenuAuthUnify(){
        return new MemorySysMenuAuthComposite();
    }
}
