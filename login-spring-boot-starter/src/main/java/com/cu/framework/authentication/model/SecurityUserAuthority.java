package com.cu.framework.authentication.model;

import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

public class SecurityUserAuthority implements GrantedAuthority {

    private final String authority;
    private final String description;

    public SecurityUserAuthority(String authority, String description) {
        this.authority = authority;
        this.description = description;
    }


    @Override
    public String getAuthority() {
        return authority;
    }

    public String getDescription() {
        return description;
    }
}
