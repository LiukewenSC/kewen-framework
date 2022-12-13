package com.kewen.framework.base.authority.model.req;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @descrpition 登录req
 * @author kewen
 * @since 2022-11-28 17:07
 */
@Data
public class LoginReq {
    @NotBlank
    private String loginInfo;
    @NotNull
    private String password;
}
