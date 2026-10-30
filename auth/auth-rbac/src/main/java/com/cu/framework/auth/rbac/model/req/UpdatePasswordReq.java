package com.cu.framework.auth.rbac.model.req;


import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 修改密码
 * @author kewen
 * @since 2024-08-22
 */
@Data
public class UpdatePasswordReq {
    /** 旧密码，未加密，必填 */
    @NotBlank
    private String oldPassword;
    /** 新密码，未加密，必填 */
    @NotBlank
    private String newPassword;
}
