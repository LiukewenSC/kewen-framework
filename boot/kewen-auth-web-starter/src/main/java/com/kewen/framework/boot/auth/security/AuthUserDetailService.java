package com.kewen.framework.boot.auth.security;

import com.kewen.framework.base.authority.model.UserCredential;
import com.kewen.framework.base.authority.support.impl.SysUserCompositeImpl;
import com.kewen.framework.base.authority.model.UserDetail;
import com.kewen.framework.boot.auth.security.model.SecurityUser;
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
public class AuthUserDetailService implements UserDetailsService {

    @Autowired
    SysUserCompositeImpl sysUserComposite;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        UserDetail userDetail = sysUserComposite.getUserDetail(username);

        UserCredential credential = sysUserComposite.getUserCredential(userDetail.getUser().getId());

        return new SecurityUser(
                userDetail,
                credential.getPassword(),
                userDetail.getUser().getUserName(),
                credential.isNonExpired(),
                credential.isNonLocked(),
                credential.isEnabled()
        );
    }
}
