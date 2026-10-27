package com.cu.framework.authentication.response;

import com.cu.framework.authentication.model.SecurityUser;
import com.cu.framework.authentication.model.SecurityUserAuthority;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 2026/02/22
 *
 * @author kewen
 * @since 1.0
 */
@Data
public class LoginSuccessResult {


    /**
     * 登录成功后返回的会话 token，取值为当前 Session ID（见 JsonAuthenticationSuccessHandler）
     */
    private String token;

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
     * 1-男 2-女 3-..，取值与 {@code SecurityUser.gender} 一致
     */
    private String gender;

    /**
     * 权限集合
     */
    private Collection<SecurityUserAuthority> authorities;

    /**
     * 本次登录时间，认证成功时取当前时间写入
     */
    private LocalDateTime loginTime;

    /**
     * 由统一认证主体投影出登录成功响应体。不含密码；
     * token 与 loginTime 由 {@code JsonAuthenticationSuccessHandler} 写入
     *
     * @param securityUser 当前认证主体
     * @return 登录成功响应体
     */
    public static LoginSuccessResult of(SecurityUser securityUser) {
        LoginSuccessResult result = new LoginSuccessResult();
        result.setName(securityUser.getName());
        result.setNickName(securityUser.getNickName());
        result.setUsername(securityUser.getUsername());
        result.setPhone(securityUser.getPhone());
        result.setEmail(securityUser.getEmail());
        result.setAvatarFileId(securityUser.getAvatarFileId());
        result.setGender(securityUser.getGender());
        result.setAuthorities(securityUser.getUserAuthorities());
        result.setLoginTime(securityUser.getLoginTime());
        return result;
    }
}
