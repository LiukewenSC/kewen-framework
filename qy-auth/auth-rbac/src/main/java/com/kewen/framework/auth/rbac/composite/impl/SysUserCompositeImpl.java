package com.kewen.framework.auth.rbac.composite.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.kewen.framework.auth.rbac.composite.model.CurrentUserSimpleAuthObject;
import com.kewen.framework.auth.rbac.composite.SysUserComposite;
import com.kewen.framework.auth.rbac.composite.mapper.SysUserUnionCompositeMapper;
import com.kewen.framework.auth.rbac.model.UserAuthObject;
import com.kewen.framework.auth.rbac.mp.entity.SysUser;
import com.kewen.framework.auth.rbac.mp.entity.SysUserCredential;
import com.kewen.framework.auth.rbac.mp.service.SysUserCredentialMpService;
import com.kewen.framework.auth.rbac.mp.service.SysUserMpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author kewen
 * @since 2024-05-11
 */
@Service
public class SysUserCompositeImpl implements SysUserComposite {

    private static final Logger log = LoggerFactory.getLogger(SysUserCompositeImpl.class);
    @Autowired
    SysUserMpService userMpService;

    @Autowired
    SysUserCredentialMpService credentialMpService;

    @Autowired
    SysUserUnionCompositeMapper unionCompositeMapper;

    @Override
    public UserAuthObject loadByUsername(String username) {

        SysUser user = userMpService.getOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username)
                        .select()
        );
        if (user == null){
            return null;
        }

        UserAuthObject userAuthObject = new UserAuthObject();
        userAuthObject.setSysUser(user);
        SysUserCredential credential = credentialMpService.getOne(
                new LambdaQueryWrapper<SysUserCredential>().eq(SysUserCredential::getUserId, user.getId())
                        .select()
        );
        if (credential == null){
            return userAuthObject;
        }
        userAuthObject.setSysUserCredential(credential);
        //查找用户权限体
        CurrentUserSimpleAuthObject authObject = unionCompositeMapper.getUserAuthObject(user.getId());
        authObject.fillUserAuth();
        userAuthObject.setAuthObject(authObject);

        return userAuthObject;
    }

    @Override
    public boolean updatePassword(String username, String newPassword) {
        SysUser user = userMpService.getOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username)
                        .select()
        );
        if (user == null){
            log.info("更新用户失败，用户不存在");
            return false;
        }
        Long id = user.getId();

        /*
        return credentialMpService.update(
                new LambdaUpdateWrapper<SysUserCredential>()
                        .set(SysUserCredential::getPassword, newPassword)
                        .eq(SysUserCredential::getUserId, user.getId())
        );
        */
       return credentialMpService.update(
                new LambdaUpdateWrapper<SysUserCredential>()
                        .eq(SysUserCredential::getUserId, user.getId())
                        .set(SysUserCredential::getPassword, newPassword)
        );


    }
}
