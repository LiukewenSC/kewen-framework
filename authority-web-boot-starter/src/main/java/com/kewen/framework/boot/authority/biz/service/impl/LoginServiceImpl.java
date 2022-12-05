package com.kewen.framework.boot.authority.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kewen.framework.boot.authority.biz.mapper.entity.SysUserInfo;
import com.kewen.framework.boot.authority.biz.model.req.LoginReq;
import com.kewen.framework.boot.authority.biz.model.resp.LoginResp;
import com.kewen.framework.boot.authority.biz.service.LoginService;
import com.kewen.framework.boot.authority.biz.service.SysRolePermissionService;
import com.kewen.framework.boot.authority.biz.service.SysUserDeptService;
import com.kewen.framework.boot.authority.biz.service.SysUserInfoService;
import com.kewen.framework.boot.authority.biz.service.SysUserPositionService;
import com.kewen.framework.boot.authority.biz.service.SysUserRoleService;
import com.kewen.framework.boot.authority.currentuser.UserDetailStore;
import com.kewen.common.model.Permission;
import com.kewen.common.model.Position;
import com.kewen.common.model.Role;
import com.kewen.common.model.User;
import com.kewen.common.model.UserDept;
import com.kewen.common.model.UserDetail;
import com.kewen.common.exception.AuthenticationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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
    private SysUserPositionService userPositionService;
    @Autowired
    private SysUserRoleService userRoleService;
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
        UserDept userDept = userDeptService.getUserDept(userId);

        //查询岗位
        List<Position> positions = userPositionService.listUserPosition(userId);

        //查询角色
        List<Role> roles = userRoleService.listUserRole(userId);

        //查询权限
        List<Integer> roleIds = roles.stream().map(Role::getId).collect(Collectors.toList());
        List<Permission> permissions = null;
        if (!CollectionUtils.isEmpty(roleIds)) {
            permissions = rolePermissionService.listRolePosition(roleIds);
        }

        return  UserDetail.builder()
                .user(new User(userId, userName))
                .dept(userDept)
                .roles(roles)
                .permissions(permissions)
                .build()
                ;
    }
}
