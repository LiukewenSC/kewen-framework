package com.cu.framework.auth.sample;

import com.cu.framework.auth.core.AuthCurrentUserContextHolder;
import com.cu.framework.auth.core.entity.FlatteningAuthority;
import com.cu.framework.auth.core.entity.CurrentUser;
import com.cu.framework.auth.rbac.composite.SysUserComposite;
import com.cu.framework.auth.rbac.model.UserAuthObject;
import lombok.Setter;

import java.util.Collection;
import java.util.Collections;

/**
 * 示例用的用户上下文：本 sample 未引入 Spring Security 登录态，用户名固定为 admin，
 * 权限集合按该用户名从 sys_user 体系实时装载
 * @author kewen
 * @since 2026-10-15
 */
@Setter
public class AuthBootSampleCurrentUserContextHolder implements AuthCurrentUserContextHolder {


    private SysUserComposite sysUserComposite;

    @Override
    public CurrentUser getUser() {
        return new CurrentUser().setUsername(getUsername());
    }

    @Override
    public Collection<FlatteningAuthority> getAuths() { UserAuthObject authObject = getUserAuthObjectByUsername(getUsername());
        if (authObject == null || authObject.getAuthInfo() == null){
            return Collections.emptyList();
        }
        return authObject.getAuthInfo().listBaseAuth();
    }

    public UserAuthObject getUserAuthObjectByUsername(String username){
        return sysUserComposite.getUserAuthObjectByUsername(username);
    }
    private String getUsername(){
        return "admin";
    }
}
