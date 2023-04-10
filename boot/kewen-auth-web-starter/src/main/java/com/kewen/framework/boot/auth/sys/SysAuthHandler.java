package com.kewen.framework.boot.auth.sys;

import com.kewen.framework.base.authority.model.SysUserDetail;
import com.kewen.framework.base.authority.mp.entity.SysUserCredential;
import com.kewen.framework.base.authority.support.SysMenuAuthComposite;
import com.kewen.framework.base.authority.support.SysUserComposite;
import com.kewen.framework.boot.auth.AuthHandler;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;

/**
 * 默认的权限处理器
 * @author kewen
 * @since 2023-04-10
 */
public class SysAuthHandler implements AuthHandler {

    @Autowired
    SysUserComposite sysUserComposite;
    @Autowired
    private SysMenuAuthComposite sysMenuAuthComposite;

    @Override
    public boolean hasAuth(Collection<String> authorities, String url) {
        return sysMenuAuthComposite.hasAuth(authorities,url);
    }

    @Override
    public Long getUserId(String username) {
        return sysUserComposite.getUserId(username);
    }

    @Override
    public SysAuthUserInfo getAuthUser(Long userId) {
        SysUserDetail sysUserDetail = sysUserComposite.getUserDetail(userId);

        return new SysAuthUserInfo(sysUserDetail);
    }

    @Override
    public SysAuthUserCredentialWrapper getCredential(Long userId) {

        SysUserCredential credential = sysUserComposite.getUserCredential(userId);

        return new SysAuthUserCredentialWrapper(credential);

    }
}
