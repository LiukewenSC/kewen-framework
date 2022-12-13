package com.kewen.framework.cloud.security.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kewen.framework.base.authority.entity.SysUserInfo;
import com.kewen.framework.base.authority.entity.SysUserRole;
import com.kewen.framework.base.authority.service.SysUserInfoService;
import com.kewen.framework.base.authority.service.SysUserRoleService;
import com.kewen.framework.cloud.security.model.SecurityAuthority;
import com.kewen.framework.cloud.security.model.SecurityUser;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author kewen
 * @descrpition 系统用户userDetail实现
 * @since 2022-12-07 17:10
 */
public class SysUserDetailService  implements UserDetailsService {

    private final SysUserInfoService sysUserInfoService;
    private final SysUserRoleService sysUserRoleService;

    public SysUserDetailService(SysUserInfoService sysUserInfoService, SysUserRoleService sysUserRoleService) {
        this.sysUserInfoService = sysUserInfoService;
        this.sysUserRoleService = sysUserRoleService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUserInfo one = sysUserInfoService.getOne(
                new LambdaQueryWrapper<SysUserInfo>()
                        .eq(SysUserInfo::getUsername, username)
        );
        if (one==null){
            return null;
        }
        SecurityUser.SecurityUserBuilder userBuilder = SecurityUser.builder()
                .id(one.getUserId())
                .name(one.getNickName())
                .password(one.getPassword())
                .username(one.getUsername());

        Long userId = one.getUserId();
        List<SysUserRole> list = sysUserRoleService.list(
                new LambdaQueryWrapper<SysUserRole>()
                        .in(SysUserRole::getUserId, userId)
        );
        if (list!=null){
            userBuilder.authorities(list.stream().map(r-> new SecurityAuthority(""+r.getRoleId(),""+r.getRoleId())).collect(Collectors.toList()));
        }
        return userBuilder.build();
    }
}