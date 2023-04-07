package com.kewen.framework.boot.auth.security;

import com.kewen.framework.base.authority.support.impl.SysUserCompositeImpl;
import com.kewen.framework.base.common.model.UserDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Arrays;

/**
 * @author kewen
 * @descrpition
 * @since 2023-04-07
 */
@Slf4j
public class AuthSecurityUserDetailService implements UserDetailsService {

    @Autowired
    SysUserCompositeImpl sysUserComposite;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDetail userDetail = sysUserComposite.getUserDetail(username);

    }
}
