package com.kewen.framework.cloud.security.config;

import com.kewen.framework.base.authority.service.SysApplicationAuthService;
import com.kewen.framework.base.authority.service.SysMenuAuthService;
import com.kewen.framework.base.authority.service.SysMenuAuthUnify;
import com.kewen.framework.base.authority.service.SysMenuService;
import com.kewen.framework.base.authority.service.SysUserDeptService;
import com.kewen.framework.base.authority.service.SysUserInfoService;
import com.kewen.framework.base.authority.service.SysUserPositionService;
import com.kewen.framework.base.authority.service.SysUserRoleService;
import com.kewen.framework.base.authority.service.impl.MemorySysMenuAuthUnify;
import com.kewen.framework.cloud.security.service.SecurityUserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author kewen
 * @descrpition
 * @since 2022-12-07 17:25
 */
@Configuration
@ComponentScan("com.kewen.framework.base.authority")
@Profile({"test","prod"})
public class SecurityBeanLoadConfig {

    @Autowired
    SysMenuService menuService;
    @Autowired
    SysMenuAuthService menuAuthService;
    @Autowired
    SysApplicationAuthService applicationAuthService;
    @Autowired
    SysUserInfoService sysUserInfoService;
    @Autowired
    SysUserRoleService sysUserRoleService;
    @Autowired
    SysUserDeptService sysUserDeptService;
    @Autowired
    SysUserPositionService sysUserPositionService;

    @Bean
    public UserDetailsService userDetailService(){
        return new SecurityUserDetailService(sysUserInfoService,sysUserDeptService,sysUserPositionService,sysUserRoleService);
    }
    @Bean
    PasswordEncoder passwordEncoder(){
        return NoOpPasswordEncoder.getInstance();
    }
    @Bean
    SysMenuAuthUnify sysMenuAuthUnify(){
        return new MemorySysMenuAuthUnify(menuService,menuAuthService,applicationAuthService);
    }

}
