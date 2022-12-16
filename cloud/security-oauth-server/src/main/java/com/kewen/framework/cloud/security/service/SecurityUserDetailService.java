package com.kewen.framework.cloud.security.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kewen.framework.base.authority.entity.SysUserInfo;
import com.kewen.framework.base.authority.service.SysUserDeptService;
import com.kewen.framework.base.authority.service.SysUserInfoService;
import com.kewen.framework.base.authority.service.SysUserPositionService;
import com.kewen.framework.base.authority.service.SysUserRoleService;
import com.kewen.framework.base.authority.utils.AuthorityConvertUtil;
import com.kewen.framework.base.common.model.Permission;
import com.kewen.framework.base.common.model.Position;
import com.kewen.framework.base.common.model.Role;
import com.kewen.framework.base.common.model.User;
import com.kewen.framework.base.common.model.UserDept;
import com.kewen.framework.base.common.model.UserDetail;
import com.kewen.framework.cloud.security.model.SecurityUser;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author kewen
 * @descrpition 系统用户userDetail实现
 * @since 2022-12-07 17:10
 */
@AllArgsConstructor
public class SecurityUserDetailService implements UserDetailsService {

    private final SysUserInfoService sysUserInfoService;
    private final SysUserDeptService userDeptService;
    private final SysUserPositionService userPositionService;
    private final SysUserRoleService userRoleService;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUserInfo userInfo = sysUserInfoService.getOne(
                new LambdaQueryWrapper<SysUserInfo>()
                        .eq(SysUserInfo::getUsername, username)
                        .or()
                        .eq(SysUserInfo::getPhone, username)
        );
        if (userInfo==null){
            return null;
        }

        UserDetail userDetail = fetchUserDetail( userInfo.getUserId(),userInfo.getNickName());
        return SecurityUser.builder()
                .id(userDetail.getUser().getId())
                .name(userDetail.getUser().getName())
                .password(userInfo.getPassword())
                .authorities(AuthorityConvertUtil.parseCurrentUser(userDetail))
                .build();
    }
    private UserDetail fetchUserDetail( Integer userId,String userName) {
        //查询机构
        UserDept userDept = userDeptService.getUserDept(userId);

        //查询岗位
        List<Position> positions = userPositionService.listUserPosition(userId);

        //查询角色
        List<Role> roles = userRoleService.listUserRole(userId);

        //查询权限
        List<Integer> roleIds = roles.stream().map(Role::getId).collect(Collectors.toList());
        List<Permission> permissions = null;
        if (!CollectionUtils.isEmpty(roleIds)) {
            //permissions = rolePermissionService.listRolePosition(roleIds);
        }

        return  UserDetail.builder()
                .user(new User(userId, userName))
                .dept(userDept)
                .positions(positions)
                .roles(roles)
                .permissions(permissions)
                .build();
    }

}
