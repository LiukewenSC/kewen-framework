package com.kewen.framework.auth.sys.support;

import com.kewen.framework.auth.core.AuthHandler;
import com.kewen.framework.auth.sys.composite.SysMenuAuthComposite;
import com.kewen.framework.auth.sys.composite.SysUserComposite;
import com.kewen.framework.auth.sys.model.SysAuthority;
import com.kewen.framework.auth.sys.model.SysUserInfo;
import com.kewen.framework.auth.sys.mp.entity.SysUserCredential;
import com.kewen.framework.auth.sys.model.SysAuthUserCredential;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * 默认的权限处理器
 * @author kewen
 * @since 2023-04-10
 */
public class SysAuthHandler implements AuthHandler<SysAuthority> {

    @Autowired
    SysUserComposite sysUserComposite;
    @Autowired
    private SysMenuAuthComposite sysMenuAuthComposite;

    @Override
    public boolean hasMenuAuth(Collection<String> authorities, String url) {
        return sysMenuAuthComposite.hasMenuAuth(authorities,url);
    }

    @Override
    public boolean hasBusinessAuth(Collection<String> auths, String module, String operate, Long businessId) {
        return sysMenuAuthComposite.hasBusinessAuth(auths, module, operate, businessId);
    }

    @Override
    public void editMenuAuthorities(Long menuId, List<SysAuthority> authority) {
        sysMenuAuthComposite.editMenuAuthorities(menuId,authority);
    }

    @Override
    public void editBusinessAuthority(Long businessId, String module, String operate, List<SysAuthority> authority) {
        sysMenuAuthComposite.editBusinessAuthority(businessId,module,operate,authority);
    }

    @Override
    public Long getUserId(String username) {
        return sysUserComposite.getUserId(username);
    }

    @Override
    public SysUserInfo getAuthUser(Long userId) {
        return sysUserComposite.getSysUserInfo(userId);
    }

    @Override
    public SysAuthUserCredential getCredential(Long userId) {

        SysUserCredential credential = sysUserComposite.getUserCredential(userId);

        return new SysAuthUserCredential(credential);

    }
}
