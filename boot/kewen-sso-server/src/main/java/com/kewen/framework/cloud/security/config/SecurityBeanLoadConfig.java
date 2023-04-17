package com.kewen.framework.cloud.security.config;

import com.kewen.framework.auth.sys.composite.SysMenuAuthComposite;
import com.kewen.framework.auth.sys.composite.impl.MemorySysMenuAuthComposite;
import com.kewen.framework.cloud.security.service.SecurityUserDetailService;
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
