package com.cu.framework.auth.sample;

import com.cu.framework.auth.core.AuthCurrentUserContextHolder;
import com.cu.framework.auth.core.entity.CurrentUser;
import com.cu.framework.auth.core.entity.IAuthObject;
import com.cu.framework.auth.rbac.composite.SysUserComposite;
import com.cu.framework.auth.rbac.model.UserAuthObject;
import lombok.Setter;

@Setter
public class AuthBootSampleCurrentUserContextHolder implements AuthCurrentUserContextHolder {


    private SysUserComposite sysUserComposite;

    @Override
    public CurrentUser getUser() {
        return new CurrentUser().setUsername(getUsername());
    }

    @Override
    public IAuthObject getAuthObject() {
        UserAuthObject authObject = getUserAuthObjectByUsername(getUsername());
        if (authObject == null){
            return null;
        }
        return authObject.getAuthObject();
    }

    public UserAuthObject getUserAuthObjectByUsername(String username){
        return sysUserComposite.getUserAuthObjectByUsername(username);
    }
    private String getUsername(){
        return "admin";
    }
}
