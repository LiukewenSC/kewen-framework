package com.cu.framework.authentication.response;

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
     * 1-男 2-女 3-..
     */
    private Integer gender;

    /**
     * 权限集合
     */
    private Collection<SecurityUserAuthority> authorities;

    /**
     * 本次登录时间，认证成功时取当前时间写入
     */
    private LocalDateTime loginTime;
}
