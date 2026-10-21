package com.cu.framework.auth.rbac.composite.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.cu.framework.auth.rbac.composite.model.CurrentUserSimpleAuthObject;
import com.cu.framework.auth.rbac.composite.SysUserComposite;
import com.cu.framework.auth.rbac.composite.mapper.SysUserUnionCompositeMapper;
import com.cu.framework.auth.rbac.model.UserAuthObject;
import com.cu.framework.auth.rbac.mp.entity.SysUser;
import com.cu.framework.auth.rbac.mp.entity.SysUserCredential;
import com.cu.framework.auth.rbac.mp.service.SysUserCredentialMpService;
import com.cu.framework.auth.rbac.mp.service.SysUserMpService;
import com.cu.framework.basic.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author kewen
 * @since 2024-05-11
 */
@Service
@RequiredArgsConstructor
public class SysUserCompositeImpl implements SysUserComposite {

    private static final Logger log = LoggerFactory.getLogger(SysUserCompositeImpl.class);
    private final SysUserMpService userMpService;

    private final SysUserCredentialMpService credentialMpService;

    private final SysUserUnionCompositeMapper unionCompositeMapper;

    @Override
    public SysUserCredential getUserCredentialByUsername(String username) {
        SysUser user = userMpService.getOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username)
        );
        if (user == null){
            throw new BizException("用户不存在");
        }
        SysUserCredential credential = credentialMpService.getOne(
                new LambdaQueryWrapper<SysUserCredential>().eq(SysUserCredential::getUserId, user.getId())
        );
        if (credential == null){
            throw new BizException("用户凭证不存在");
        }
        return credential;
    }

    @Override
    public UserAuthObject getUserAuthObjectByUsername(String username) {

        SysUser user = userMpService.getOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username)
        );
        if (user == null){
            return null;
        }

        UserAuthObject userAuthObject = new UserAuthObject();
        userAuthObject.setSysUser(user);
        SysUserCredential credential = credentialMpService.getOne(
                new LambdaQueryWrapper<SysUserCredential>().eq(SysUserCredential::getUserId, user.getId())
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
    public boolean updatePassword(String username, String encryptedNewPassword) {
        SysUser user = userMpService.getOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username)
        );
        if (user == null){
            log.info("更新用户失败，用户不存在");
            return false;
        }

       return credentialMpService.update(
                new LambdaUpdateWrapper<SysUserCredential>()
                        .eq(SysUserCredential::getUserId, user.getId())
                        .set(SysUserCredential::getPassword, encryptedNewPassword)
        );


    }
}
