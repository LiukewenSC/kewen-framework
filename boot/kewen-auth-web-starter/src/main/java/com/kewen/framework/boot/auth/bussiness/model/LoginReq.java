package com.kewen.framework.boot.auth.bussiness.model;

import lombok.Data;

/**
 * @descrpition 登录req
 * @author kewen
 * @since 2022-11-28 17:07
 */
@Data
public class LoginReq {
    private String loginInfo;
    private String password;
}
