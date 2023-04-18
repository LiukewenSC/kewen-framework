package com.kewen.framework.boot.auth.security.model;

import com.kewen.framework.auth.core.model.AuthEntity;
import com.kewen.framework.auth.core.model.AuthUserInfo;
import com.kewen.framework.common.core.model.IUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @descrpition
 * @author kewen
 * @since 2023-04-07
 */
public class SecurityUser<E extends AuthEntity,T extends AuthUserInfo<E>>  implements UserDetails, IUser {

    private String token;
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
    public T getAuthUserInfo() {
        return authUserInfo;
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<E> authorities = authUserInfo.authorities();
        return authorities.stream().map(a->new SimpleGrantedAuthority(a.getAuth())).collect(Collectors.toList());
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
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