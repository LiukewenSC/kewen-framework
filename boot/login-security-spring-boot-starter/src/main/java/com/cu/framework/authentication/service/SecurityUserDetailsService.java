package com.cu.framework.authentication.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * 框架用户详情服务接口，组合 UserDetailsService 与 UserDetailsPasswordService，
 * 统一提供按用户名加载用户与更新用户密码的能力
 * @author kewen
 * @since 2026-02-12
 */
public interface SecurityUserDetailsService extends UserDetailsService, UserDetailsPasswordService {
    @Override
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;

    /**
     * 更新密码，主要用于刷新用户旧的密钥算法，登陆一次之后重置密码
     * @param user the user to modify the password for
     * @param encryptedNewPassword the password to change to, encoded by the configured
     * {@code PasswordEncoder}
     * @return
     */
    @Override
    UserDetails updatePassword(UserDetails user, String encryptedNewPassword);
}
