package com.cu.framework.authentication.model;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 安全用户模型，Spring Security UserDetails 的实现，同时实现 CurrentUser 与 AuthenticatedPrincipal；
 * 承载当前登录用户的基本信息、账号状态（启用/锁定/凭证过期）与权限对象，供认证与用户上下文使用
 * @author kewen
 * @since 2026-02-12
 */
@Data
@Accessors(chain = true)
public class SecurityUser implements UserDetails, AuthenticatedPrincipal, Serializable {

    @Serial
    private static final long serialVersionUID = 5276930894128725384L;

    private String token;

    /**
     * 主键id
     */
    private Long id;

    /**
     * 姓名
     */
    private String name;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 用户名
     */
    private String username;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 头像
     */
    private Long avatarFileId;

    /**
     * 密码
     */
    private String password;

    /**
     * 1-男 2-女 3-..
     * <p>String 而非 Integer：单点登录的子类型要实现 {@code OidcUser}，
     * 而该接口的父接口 StandardClaimAccessor 提供了 {@code String getGender()}，
     * 同名方法返回类型不同会导致无法继承本类
     */
    private String gender;

    /**
     * 权限集合
     */
    private List<SecurityUserAuthority> userAuthorities;

    /**
     * 本次登录时间
     */
    private LocalDateTime loginTime;

    /**
     * 凭证过期时间 每次修改密码应修改过期时间 ， 为空表示系统无过期时间设定
     */
    private LocalDateTime passwordExpiredTime;

    /**
     * 账号锁定截止时间，为空或早于当前时间则为不锁定
     */
    private LocalDateTime accountLockedDeadline;

    /**
     * 账号是否启用
     */
    private Boolean enabled;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;


    /**
     * 判断给定时间点是否未到期：为 null（表示不设限）或晚于当前时间视为未到期
     * @param localDateTime 到期时间点
     * @return 未到期返回 true
     */
    private boolean isNonExpired(LocalDateTime localDateTime) {
        return localDateTime == null || LocalDateTime.now().isBefore(localDateTime);
    }

    @Override
    public String getPassword() {
        return password;
    }

    /**
     * 账号是否未过期：本实现不做账号过期控制，恒返回 true
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 账号是否未锁定：锁定截止时间 accountLockedDeadline 为空或晚于当前时间即为未锁定
     */
    @Override
    public boolean isAccountNonLocked() {
        return isNonExpired(this.accountLockedDeadline);
    }

    /**
     * 凭证是否未过期：密码过期时间 passwordExpiredTime 为空或晚于当前时间即为未过期
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return isNonExpired(this.passwordExpiredTime);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Spring Security 需要的授权集合
     * @return 授权集合，userAuthorities 未设置时返回空集合
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return userAuthorities == null ? Collections.emptyList() : userAuthorities;
    }
}
