package com.kewen.framework.cloud.security.service;

import com.kewen.framework.base.authority.mp.entity.SysUserCredential;
import com.kewen.framework.base.authority.support.SysUserComposite;
import com.kewen.framework.base.authority.utils.AuthorityConvertUtil;
import com.kewen.framework.base.authority.model.SysUserDetail;
import com.kewen.framework.cloud.security.model.SecurityUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * @author kewen
 * @descrpition 系统用户userDetail实现
 * @since 2022-12-07 17:10
 */
@Component
public class SecurityUserDetailService implements UserDetailsService {

    @Autowired
    SysUserComposite sysUserComposite;



    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Long userId = sysUserComposite.getUserId(username);
        SysUserDetail sysUserDetail = sysUserComposite.getUserDetail(userId);

        SysUserCredential userCredential = sysUserComposite.getUserCredential(userId);

        return SecurityUser.builder()
                .id(sysUserDetail.getId())
                .name(sysUserDetail.getName())
                .password(userCredential.getPassword())
                .authorities(AuthorityConvertUtil.parseCurrentUser(sysUserDetail))
                .build();
    }

}
