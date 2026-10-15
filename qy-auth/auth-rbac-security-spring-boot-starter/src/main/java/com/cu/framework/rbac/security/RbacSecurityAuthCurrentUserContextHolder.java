package com.cu.framework.rbac.security;

import com.cu.framework.auth.core.entity.CurrentUser;
import com.cu.framework.auth.core.AuthCurrentUserContextHolder;
import com.cu.framework.auth.core.entity.IAuthObject;
import com.cu.framework.auth.rbac.model.UserAuthObject;
import com.cu.framework.authentication.SecurityUserContext;
import com.cu.framework.authentication.model.SecurityUser;
import lombok.Setter;

@Setter
public class RbacSecurityAuthCurrentUserContextHolder implements AuthCurrentUserContextHolder {

    private RbacSecurityUserDetailsService rbacSecurityUserDetailsService;

    @Override
    public CurrentUser getUser() {
        SecurityUser currentUser = SecurityUserContext.getCurrentUser();
        return new CurrentUser().setUsername(currentUser.getUsername());
    }

    @Override
    public IAuthObject getAuthObject() {
        SecurityUser currentUser = SecurityUserContext.getCurrentUser();
        UserAuthObject authObject = rbacSecurityUserDetailsService.getUserAuthObjectByUsername(currentUser.getUsername());
        if (authObject ==null){
            return null;
        }
        return authObject.getAuthObject();
    }
}
