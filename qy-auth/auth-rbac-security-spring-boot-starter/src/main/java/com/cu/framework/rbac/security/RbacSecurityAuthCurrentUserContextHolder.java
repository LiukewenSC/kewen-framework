package com.cu.framework.rbac.security;

import com.cu.framework.auth.core.entity.FlatteningAuthority;
import com.cu.framework.auth.core.entity.CurrentUser;
import com.cu.framework.auth.core.AuthCurrentUserContextHolder;
import com.cu.framework.authentication.SecurityUserContext;
import com.cu.framework.authentication.model.SecurityUser;

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
    public Collection<FlatteningAuthority> getAuths() {
        SecurityUser currentUser = SecurityUserContext.getCurrentUser();
        if (currentUser == null|| currentUser.getUserAuthorities() ==null){
            return Collections.emptyList();
        }
        return currentUser.getUserAuthorities().stream().map(a -> new FlatteningAuthority(a.getAuthority(), a.getDescription())).collect(Collectors.toList());
    }
}
