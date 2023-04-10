package com.kewen.framework.boot.auth.security.model;

import com.kewen.framework.base.common.model.IUser;
import com.kewen.framework.boot.auth.AuthUserInfo;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * @descrpition
 * @author kewen
 * @since 2023-04-07
 */
public class SecurityUser<T extends AuthUserInfo>  implements UserDetails, IUser {

    private final T authUserInfo;
    private final String password;
    private final String username;
    private final boolean isNonExpired;
    private final boolean isNonLocked;
    private final boolean enabled;

    public SecurityUser(T authUserInfo, String password, String username, boolean isNonExpired, boolean isNonLocked, boolean enabled) {
        this.authUserInfo = authUserInfo;
        this.password = password;
        this.username = username;
        this.isNonExpired = isNonExpired;
        this.isNonLocked = isNonLocked;
        this.enabled = enabled;
    }
    public AuthUserInfo getAuthUserInfo() {
        return authUserInfo;
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
        return getAuthUserInfo().getUserId();
    }

    @Override
    public String getUserName() {
        return getAuthUserInfo().getUserName();
    }
}