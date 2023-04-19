package com.kewen.framework.auth.sys.model.req;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 
 * @author kewen
 * @since 2023-04-19
 */
@Data
public class UpdatePasswordReq {
    @NotNull
    private Long id;
    @NotNull
    private String oldPassword;
    @NotNull
    private String newPassword;
    private String remark;
}
