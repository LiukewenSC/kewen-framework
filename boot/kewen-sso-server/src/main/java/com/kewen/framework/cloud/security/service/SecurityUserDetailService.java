package com.kewen.framework.cloud.security.service;

import com.kewen.framework.base.authority.support.SysUserComposite;
import com.kewen.framework.base.authority.utils.AuthorityConvertUtil;
import com.kewen.framework.base.common.model.Position;
import com.kewen.framework.base.common.model.Role;
import com.kewen.framework.base.common.model.User;
import com.kewen.framework.base.common.model.UserDept;
import com.kewen.framework.base.common.model.UserDetail;
import com.kewen.framework.cloud.security.model.SecurityUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;

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

        UserDetail userDetail = sysUserComposite.getUserDetail(username);

        return SecurityUser.builder()
                .id(userDetail.getUser().getId())
                .name(userDetail.getUser().getName())
                .password(userInfo.getPassword())
                .authorities(AuthorityConvertUtil.parseCurrentUser(userDetail))
                .build();
    }

}
