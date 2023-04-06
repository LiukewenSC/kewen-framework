package com.kewen.framework.boot.authority.config;

import com.kewen.framework.base.authority.service.SysApplicationAuthService;
import com.kewen.framework.base.authority.service.SysMenuAuthService;
import com.kewen.framework.base.authority.service.SysMenuAuthUnify;
import com.kewen.framework.base.authority.service.SysMenuService;
import com.kewen.framework.base.authority.service.impl.MemorySysMenuAuthUnify;
import com.kewen.framework.base.authority.service.impl.SysApplicationAuthServiceImpl;
import com.kewen.framework.base.authority.service.impl.SysMenuServiceImpl;
import com.kewen.framework.boot.authority.advance.menucheck.AuthMenuInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
    SysMenuAuthService menuAuthService;
    @Autowired
    SysMenuService sysMenuService;
    @Autowired
    SysApplicationAuthService sysApplicationAuthService;

    @Bean
    public AuthMenuInterceptor authMenuInterceptor(SysMenuAuthUnify menuService){
        return new AuthMenuInterceptor(menuService);
    }

    @Bean
    public SysMenuAuthUnify sysMenuAuthUnify(){
        return new MemorySysMenuAuthUnify( sysMenuService,  menuAuthService,sysApplicationAuthService);
    }
}
