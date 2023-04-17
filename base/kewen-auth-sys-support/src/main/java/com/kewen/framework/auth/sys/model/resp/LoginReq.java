package com.kewen.framework.auth.sys.model.resp;

import lombok.Data;

/**
 * @descrpition 登录req
 * @author kewen
 * @since 2022-11-28 17:07
 */
@Data
public class LoginReq {
    private String username;
    private String password;
}
