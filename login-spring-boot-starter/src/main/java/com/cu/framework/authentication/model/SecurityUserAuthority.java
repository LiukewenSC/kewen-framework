package com.cu.framework.authentication.model;

import org.springframework.security.core.GrantedAuthority;

public class SecurityUserAuthority implements GrantedAuthority {

    private String authority;

    @Override
    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }
}
