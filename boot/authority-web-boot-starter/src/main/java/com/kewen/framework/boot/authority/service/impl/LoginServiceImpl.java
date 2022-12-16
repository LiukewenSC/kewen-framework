package com.kewen.framework.boot.authority.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kewen.framework.base.authority.entity.SysDept;
import com.kewen.framework.base.authority.entity.SysRole;
import com.kewen.framework.base.authority.entity.SysUserDept;
import com.kewen.framework.base.authority.entity.SysUserInfo;
import com.kewen.framework.base.authority.entity.SysUserRole;
import com.kewen.framework.base.authority.service.SysDeptService;
import com.kewen.framework.base.authority.service.SysRolePermissionService;
import com.kewen.framework.base.authority.service.SysRoleService;
import com.kewen.framework.base.authority.service.SysUserDeptService;
import com.kewen.framework.base.authority.service.SysUserInfoService;
import com.kewen.framework.base.authority.service.SysUserPositionService;
import com.kewen.framework.base.authority.service.SysUserRoleService;
import com.kewen.framework.base.common.model.Dept;
import com.kewen.framework.base.common.model.DeptPrimary;
import com.kewen.framework.base.common.utils.BeanUtil;
import com.kewen.framework.boot.authority.model.LoginReq;
import com.kewen.framework.boot.authority.model.LoginResp;
import com.kewen.framework.boot.authority.service.LoginService;
import com.kewen.framework.boot.authority.currentuser.UserDetailStore;
import com.kewen.framework.base.common.model.Role;
import com.kewen.framework.base.common.model.User;
import com.kewen.framework.base.common.model.UserDept;
import com.kewen.framework.base.common.model.UserDetail;
import com.kewen.framework.base.common.exception.AuthenticationException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author kewen
 * @descrpition
 * @since 2022-11-28 17:18
 */
@Service
public class LoginServiceImpl implements LoginService {
    @Autowired
    private SysUserInfoService userInfoService;
    @Autowired
    private UserDetailStore userDetailStore;

    @Autowired
    private SysUserDeptService userDeptService;
    @Autowired
    private SysDeptService deptService;
    @Autowired
    private SysUserPositionService userPositionService;
    @Autowired
    private SysUserRoleService userRoleService;
    @Autowired
    private SysRoleService roleService;
    @Autowired
    private SysRolePermissionService rolePermissionService;

    @Override
    public LoginResp login(LoginReq loginReq) {
        String loginInfo = loginReq.getLoginInfo();

        //查询用户
        SysUserInfo userInfo = userInfoService.getOne(
                new LambdaQueryWrapper<SysUserInfo>()
                        .eq(SysUserInfo::getUsername, loginInfo)
                        .or()
                        .eq(SysUserInfo::getPhone, loginInfo)
        );
        if (userInfo == null) {
            throw new AuthenticationException("用户不存在");
        }

        //todo 校验密码，此处应该抽象密码验证器
        if (!userInfo.getPassword().equals(loginReq.getPassword())) {
            throw new AuthenticationException("密码不匹配");
        }

        UserDetail userDetail = fetchUserDetail( userInfo.getUserId(),userInfo.getNickName());

        LoginResp loginResp = userDetailStore.saveUserDetail(userDetail);

        return loginResp;
    }

    private UserDetail fetchUserDetail( Integer userId,String userName) {
        //查询机构
        SysUserDept sysUserDept = userDeptService.getOne(
                new LambdaQueryWrapper<SysUserDept>()
                        .eq(SysUserDept::getUserId, userId)
        );
        List<SysDept> sysDepts = deptService.list(
                new LambdaQueryWrapper<SysDept>()
                        .in(SysDept::getId, sysUserDept.getDeptId())
        );
        SysDept sysDept = sysDepts.remove(0);
        UserDept userDept = new UserDept(
                new DeptPrimary(sysDept.getId(),sysDept.getName(),true),
                BeanUtil.toList(sysDepts, Dept.class)
                );

        //查询岗位
       //List<Position> positions = userPositionService.listUserPosition(userId);

        //查询角色
        List<Role> roles = null;

        List<SysUserRole> roleids = userRoleService.list(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId)
        );
        if (CollectionUtils.isNotEmpty(roleids)){
            List<SysRole> list = roleService.list(
                    new LambdaQueryWrapper<SysRole>().in(SysRole::getId, roleids)
            );
            if (CollectionUtils.isNotEmpty(list)){
                roles=list.stream().map(r->new Role(r.getId(),r.getName())).collect(Collectors.toList());
            }
        }

        //查询权限
        //List<Integer> roleIds = roles.stream().map(Role::getId).collect(Collectors.toList());
        //List<Permission> permissions = null;
        //if (!CollectionUtils.isEmpty(roleIds)) {
        //    permissions = rolePermissionService.listRolePosition(roleIds);
        //}

        return  UserDetail.builder()
                .user(new User(userId, userName))
                .dept(userDept)
                .roles(roles)
                //.permissions(permissions)
                .build()
                ;
    }
}
