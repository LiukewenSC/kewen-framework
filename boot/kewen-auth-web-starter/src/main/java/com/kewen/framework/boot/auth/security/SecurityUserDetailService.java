package com.kewen.framework.boot.auth.security;

import com.kewen.framework.boot.auth.security.model.SecurityUser;
import com.kewen.framework.boot.auth.AuthHandler;
import com.kewen.framework.boot.auth.AuthUserCredential;
import com.kewen.framework.boot.auth.AuthUserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * @author kewen
 * @descrpition
 * @since 2023-04-07
 */
@Slf4j
public class SecurityUserDetailService implements UserDetailsService {

    @Autowired
    AuthHandler authHandler;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Long userId = authHandler.getUserId(username);
        AuthUserInfo userDetail = authHandler.getAuthUser(userId);
        AuthUserCredential credential = authHandler.getCredential(userId);

        return new SecurityUser(
                userDetail,
                credential.getPassword(),
                userDetail.getUserName(),
                credential.isNonExpired(),
                credential.isNonLocked(),
                credential.isEnabled()
        );
    }
}
