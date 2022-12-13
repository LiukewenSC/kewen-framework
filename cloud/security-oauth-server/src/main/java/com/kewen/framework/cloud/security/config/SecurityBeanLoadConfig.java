package com.kewen.framework.cloud.security.config;

import com.kewen.framework.base.authority.service.SysUserInfoService;
import com.kewen.framework.base.authority.service.SysUserRoleService;
import com.kewen.framework.cloud.security.service.SysUserDetailService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
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
public class SecurityBeanLoadConfig {

    @Bean
    public UserDetailsService userDetailService(SysUserInfoService sysUserInfoService, SysUserRoleService sysUserRoleService){
        return new SysUserDetailService(sysUserInfoService, sysUserRoleService);
    }
    @Bean
    PasswordEncoder passwordEncoder(){
        return NoOpPasswordEncoder.getInstance();
    }

}
