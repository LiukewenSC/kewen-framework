package com.kewen.framework.boot.authority.config;

import com.kewen.framework.base.authority.service.SysMenuAuthService;
import com.kewen.framework.base.authority.service.SysMenuService;
import com.kewen.framework.boot.authority.advance.menucheck.AuthMenuInterceptor;
import com.kewen.framework.boot.authority.service.SysMenuAuthUnify;
import com.kewen.framework.boot.authority.service.impl.MemorySysMenuAuthUnify;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author kewen
 * @descrpition
 * @since 2022-12-07 22:36
 */
@Configuration
public class AuthorityWebBeanLoadConfig {
    @Autowired
    SysMenuService menuService;
    @Autowired
    SysMenuAuthService menuAuthService;
    @Bean
    public AuthMenuInterceptor authMenuInterceptor(SysMenuAuthUnify menuService){
        return new AuthMenuInterceptor(menuService);
    }
    @Bean
    public SysMenuAuthUnify sysMenuAuthUnify(){
        return new MemorySysMenuAuthUnify( menuService,  menuAuthService);
    }
}
