package com.cu.framework.auth.rbac.security;

import com.cu.framework.authentication.model.SecurityUser;
import com.cu.framework.authentication.model.SecurityUserAuthority;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 当前登录用户接口的响应体：只暴露页面需要的身份字段，不包含密码或密码哈希。
 *
 * @author kewen
 * @since 2026-10-27
 */
@Data
public class RbacCurrentUserResp {

    /** 用户主键 */
    private Long id;
    /** 姓名 */
    private String name;
    /** 用户昵称 */
    private String nickName;
    /** 用户名 */
    private String username;
    /** 手机号 */
    private String phone;
    /** 邮箱 */
    private String email;
    /** 头像文件 ID */
    private Long avatarFileId;
    /** 性别，取值与 SecurityUser.gender 一致 */
    private String gender;
    /** 权限集合 */
    private List<SecurityUserAuthority> authorities;
    /** 本次登录时间 */
    private LocalDateTime loginTime;

    /**
     * 从统一认证主体投影出当前用户响应，不复制 password 字段
     *
     * @param user 当前认证主体
     * @return 当前用户响应
     */
    public static RbacCurrentUserResp of(SecurityUser user) {
        RbacCurrentUserResp resp = new RbacCurrentUserResp();
        resp.setId(user.getId());
        resp.setName(user.getName());
        resp.setNickName(user.getNickName());
        resp.setUsername(user.getUsername());
        resp.setPhone(user.getPhone());
        resp.setEmail(user.getEmail());
        resp.setAvatarFileId(user.getAvatarFileId());
        resp.setGender(user.getGender());
        resp.setAuthorities(user.getUserAuthorities());
        resp.setLoginTime(user.getLoginTime());
        return resp;
    }
}
