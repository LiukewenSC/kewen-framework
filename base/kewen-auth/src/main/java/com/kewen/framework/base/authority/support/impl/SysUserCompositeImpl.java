package com.kewen.framework.base.authority.support.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kewen.framework.base.authority.model.*;
import com.kewen.framework.base.authority.mp.entity.SysUser;
import com.kewen.framework.base.authority.mp.entity.SysUserCredential;
import com.kewen.framework.base.authority.mp.service.SysUserCredentialMpService;
import com.kewen.framework.base.authority.mp.service.SysUserMpService;
import com.kewen.framework.base.authority.support.mapper.SysUserCompositeMapper;
import com.kewen.framework.base.authority.mp.service.SysMenuAuthMpService;
import com.kewen.framework.base.authority.support.SysUserComposite;
import com.kewen.framework.base.common.exception.AuthenticationException;
import com.kewen.framework.base.common.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @descrpition 系统用户相关联合查询
 * @author kewen
 * @since 2023-04-07
 */
@Service
public class SysUserCompositeImpl implements SysUserComposite {

    @Autowired
    SysUserCompositeMapper userCompositeMapper;
    @Autowired
    SysMenuAuthMpService sysMenuAuthMpService;
    @Autowired
    SysUserMpService userMpService;
    @Autowired
    SysUserCredentialMpService userCredentialsMpService;


    @Override
    public UserDept getUserDept(Long userId) {

        List<DeptPrimary> depts = userCompositeMapper.listUserDept(userId);

        if (depts==null){
            return null;
        }
        boolean hasPrimary=false;
        Dept deptPrimary=null;
        List<Dept> extras = new ArrayList<>();

        Iterator<DeptPrimary> iterator = depts.iterator();
        while (iterator.hasNext()){
            DeptPrimary next = iterator.next();
            if (next.getIsPrimary()){
                hasPrimary = true;
                deptPrimary=next;
                iterator.remove();
            } else {
                extras.add(next);
            }
        }

        //数据为空返回null
        if (deptPrimary == null && extras.size()==0){
            return null;
        }

        //如果没有主要机构，取最后一个，移除原有
        if (!hasPrimary && extras.size()>0){
            deptPrimary = extras.remove(extras.size() - 1);
        }

        UserDept userDept = new UserDept();
        userDept.setPrimary(deptPrimary);
        if (!CollectionUtils.isEmpty(extras)){
            userDept.setExtras(extras);
        }
        return userDept;
    }

    @Override
    public List<Role> listUserRole(Long userId) {
        List<Role> roles = userCompositeMapper.listUserRole(userId);
        return roles==null? Collections.emptyList():roles;
    }

    @Override
    public List<Position> listUserPosition(Long userId) {
        List<Position> positions = userCompositeMapper.listUserPosition(userId);
        return positions==null? Collections.emptyList():positions;
    }




    @Override
    public UserDetail getUserDetail(String loginInfo) {
        SysUser sysUser = userMpService.getOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, loginInfo)
                        .or()
                        .eq(SysUser::getPhone, loginInfo)
        );
        if (sysUser == null) {
            throw new AuthenticationException("用户不存在");
        }
        Long userId = sysUser.getId();

        //查询机构
        UserDept userDept = getUserDept(userId);

        //查询岗位
        List<Position> positions = listUserPosition(userId);

        //查询角色
        List<Role> roles = listUserRole(userId);


        return UserDetail.builder()
                .user(new User(sysUser.getId(),sysUser.getName()))
                .dept(userDept)
                .positions(positions)
                .roles(roles)
                .build()
                ;
    }

    @Override
    public UserCredential getUserCredential(Long userId){

        SysUserCredential credential = userCredentialsMpService.getOne(
                new LambdaQueryWrapper<SysUserCredential>()
                        .eq(SysUserCredential::getUserId, userId)
        );
        return new UserCredential() {

            @Override
            public String getPassword() {
                return credential.getPassword();
            }

            @Override
            public boolean isNonExpired() {
                LocalDateTime expiredTime = credential.getPasswordExpiredTime();
                // expiredTime == null 表示系统未设定过期时间，永久有效
                return expiredTime == null || LocalDateTime.now().isAfter(expiredTime);
            }

            @Override
            public boolean isNonLocked() {
                LocalDateTime lockedDeadline = credential.getAccountLockedDeadline();

                return lockedDeadline == null || LocalDateTime.now().isAfter(lockedDeadline);
            }

            @Override
            public boolean isEnabled() {
                return false;
            }
        };
    }

}
