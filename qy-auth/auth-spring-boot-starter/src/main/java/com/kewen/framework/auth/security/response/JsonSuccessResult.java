package com.kewen.framework.auth.security.response;

import com.kewen.framework.auth.core.entity.IAuthObject;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 2026/02/22
 *
 * @author kewen
 * @since 1.0
 */
@Data
public class JsonSuccessResult {


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
    private IAuthObject authObject;

    private LocalDateTime loginTime;
}
