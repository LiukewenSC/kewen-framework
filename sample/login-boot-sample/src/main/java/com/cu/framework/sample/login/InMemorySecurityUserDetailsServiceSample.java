package com.cu.framework.sample.login;

import com.cu.framework.authentication.model.SecurityUser;
import com.cu.framework.authentication.model.SecurityUserAuthority;
import com.cu.framework.authentication.service.SecurityUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * demo 用户源，进程内 Map 存储，替换真实工程中的数据库/远程用户服务
 * <p>
 * demo 账号：admin / admin123（ROLE_ADMIN, ROLE_USER）、demo / demo123（ROLE_USER）
 *
 * @author kewen
 * @since 2026-10-15
 */
@Component
public class InMemorySecurityUserDetailsServiceSample implements SecurityUserDetailsService {

    /** 用户名到用户的内存表；密码存的是密文，与真实库中的用法一致 */
    private final Map<String, SecurityUser> users = new LinkedHashMap<>();

    public InMemorySecurityUserDetailsServiceSample(PasswordEncoder passwordEncoder) {
        addUser(passwordEncoder, "admin", "admin123", "管理员", Arrays.asList("ROLE_ADMIN", "ROLE_USER"));
        addUser(passwordEncoder, "demo", "demo123", "演示用户", Arrays.asList("ROLE_USER"));
    }

    private void addUser(PasswordEncoder passwordEncoder, String username, String rawPassword, String name,
                         List<String> authorities) {
        List<SecurityUserAuthority> userAuthorities = new ArrayList<>();
        for (String authority : authorities) {
            SecurityUserAuthority securityUserAuthority = new SecurityUserAuthority();
            securityUserAuthority.setAuthority(authority);
            userAuthorities.add(securityUserAuthority);
        }
        // enabled 与 userAuthorities 必须非空：SecurityUser 直接拆箱 enabled、直接把 userAuthorities 当作 GrantedAuthority 集合返回
        SecurityUser user = new SecurityUser()
                .setId((long) users.size() + 1)
                .setUsername(username)
                .setName(name)
                .setNickName(name)
                .setPassword(passwordEncoder.encode(rawPassword))
                .setEnabled(true)
                .setUserAuthorities(userAuthorities);
        users.put(username, user);
    }

    /**
     * 按用户名取内存用户，取不到时抛 UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SecurityUser user = users.get(username);
        if (user == null) {
            throw new UsernameNotFoundException("未找到用户");
        }
        return user;
    }

    /**
     * 把已加密的新密码写回内存用户
     */
    @Override
    public UserDetails updatePassword(UserDetails user, String newPassword) {
        SecurityUser securityUser = users.get(user.getUsername());
        if (securityUser != null) {
            securityUser.setPassword(newPassword);
        }
        return securityUser;
    }
}