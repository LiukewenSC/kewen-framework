package com.kewen.framework.boot.auth.web.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kewen.framework.base.authority.mp.entity.SysDept;
import com.kewen.framework.base.authority.mp.entity.SysRole;
import com.kewen.framework.base.authority.mp.entity.SysUserDept;
import com.kewen.framework.base.authority.mp.entity.SysUserInfo;
import com.kewen.framework.base.authority.mp.entity.SysUserRole;
import com.kewen.framework.base.authority.mp.service.SysDeptMpService;
import com.kewen.framework.base.authority.mp.service.SysRoleMpService;
import com.kewen.framework.base.authority.mp.service.SysUserDeptMpService;
import com.kewen.framework.base.authority.mp.service.SysUserInfoMpService;
import com.kewen.framework.base.authority.mp.service.SysUserPositionMpService;
import com.kewen.framework.base.authority.mp.service.SysUserRoleMpService;
import com.kewen.framework.base.authority.support.SysUserComposite;
import com.kewen.framework.base.common.model.Dept;
import com.kewen.framework.base.common.model.DeptPrimary;
import com.kewen.framework.base.common.utils.BeanUtil;
import com.kewen.framework.boot.auth.bussiness.model.LoginReq;
import com.kewen.framework.boot.auth.bussiness.model.LoginResp;
import com.kewen.framework.boot.auth.web.service.LoginService;
import com.kewen.framework.boot.auth.web.currentuser.UserDetailStore;
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
    SysUserComposite sysUserComposite;




    @Override
    public LoginResp login(LoginReq loginReq) {
        String loginInfo = loginReq.getLoginInfo();

        //查询用户


        UserDetail userDetail = fetchUserDetail(userInfo.getUserId(), userInfo.getNickName());

        LoginResp loginResp = userDetailStore.saveUserDetail(userDetail);

        return loginResp;
    }

    private UserDetail fetchUserDetail(Long userId, String userName) {

    }
}
