package com.kewen.framework.boot.auth.security.model;

import com.kewen.framework.base.authority.model.UserDetail;
import com.kewen.framework.base.common.model.IUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

/**
 * @descrpition
 * @author kewen
 * @since 2023-04-07
 */
public class SecurityUser implements UserDetails, IUser {

    private final UserDetail UserDetail;
    private final String password;
    private final String username;
    private final boolean isNonExpired;
    private final boolean isNonLocked;
    private final boolean enabled;

    public SecurityUser(UserDetail userDetail,String password, String username, boolean isNonExpired, boolean isNonLocked, boolean enabled) {
        this.UserDetail = userDetail;
        this.password = password;
        this.username = username;
        this.isNonExpired = isNonExpired;
        this.isNonLocked = isNonLocked;
        this.enabled = enabled;
    }
    public UserDetail getUserDetail() {
        return UserDetail;
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return isNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }


    @Override
    public Long getUserId() {
        return getUserDetail().getUser().getUserId();
    }

    @Override
    public String getUserName() {
        return getUserDetail().getUser().getUserName();
    }
}