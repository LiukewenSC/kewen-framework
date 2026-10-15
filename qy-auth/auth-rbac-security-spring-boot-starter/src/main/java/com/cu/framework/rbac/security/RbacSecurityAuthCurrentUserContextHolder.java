package com.cu.framework.rbac.security;

import com.cu.framework.auth.core.entity.BaseAuth;
import com.cu.framework.auth.core.entity.CurrentUser;
import com.cu.framework.auth.core.AuthCurrentUserContextHolder;
import com.cu.framework.auth.core.entity.IAuthObject;
import com.cu.framework.auth.rbac.model.UserAuthObject;
import com.cu.framework.authentication.SecurityUserContext;
import com.cu.framework.authentication.model.SecurityUser;
import lombok.Setter;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

//@Setter
public class RbacSecurityAuthCurrentUserContextHolder implements AuthCurrentUserContextHolder {

    //private RbacSecurityUserDetailsService rbacSecurityUserDetailsService;

    @Override
    public CurrentUser getUser() {
        SecurityUser currentUser = SecurityUserContext.getCurrentUser();
        return new CurrentUser().setUsername(currentUser.getUsername());
    }

    @Override
    public Collection<BaseAuth> getAuths() {
        SecurityUser currentUser = SecurityUserContext.getCurrentUser();
        if (currentUser == null|| currentUser.getUserAuthorities() ==null){
            return Collections.emptyList();
        }
        return currentUser.getUserAuthorities().stream().map(a -> new BaseAuth(a.getAuthority(), a.getDescription())).collect(Collectors.toList());
    }
}
