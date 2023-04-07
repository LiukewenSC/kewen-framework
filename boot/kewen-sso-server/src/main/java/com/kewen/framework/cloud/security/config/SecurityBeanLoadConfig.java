package com.kewen.framework.cloud.security.config;

import com.kewen.framework.base.authority.mp.service.SysApplicationAuthMpService;
import com.kewen.framework.base.authority.mp.service.SysMenuAuthMpService;
import com.kewen.framework.base.authority.mp.service.SysMenuMpService;
import com.kewen.framework.base.authority.mp.service.SysUserDeptMpService;
import com.kewen.framework.base.authority.mp.service.SysUserInfoMpService;
import com.kewen.framework.base.authority.mp.service.SysUserPositionMpService;
import com.kewen.framework.base.authority.mp.service.SysUserRoleMpService;
import com.kewen.framework.base.authority.support.SysMenuAuthComposite;
import com.kewen.framework.base.authority.support.impl.MemorySysMenuAuthComposite;
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
    SysMenuMpService menuService;
    @Autowired
    SysMenuAuthMpService menuAuthService;
    @Autowired
    SysApplicationAuthMpService applicationAuthService;
    @Autowired
    SysUserInfoMpService sysUserInfoService;
    @Autowired
    SysUserRoleMpService sysUserRoleService;
    @Autowired
    SysUserDeptMpService sysUserDeptService;
    @Autowired
    SysUserPositionMpService sysUserPositionService;

    @Bean
    public UserDetailsService userDetailService(){
        return new SecurityUserDetailService();
    }
    @Bean
    PasswordEncoder passwordEncoder(){
        return NoOpPasswordEncoder.getInstance();
    }
    @Bean
    SysMenuAuthComposite sysMenuAuthUnify(){
        return new MemorySysMenuAuthComposite();
    }

}
