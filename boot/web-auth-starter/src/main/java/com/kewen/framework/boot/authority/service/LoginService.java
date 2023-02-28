package com.kewen.framework.boot.authority.service;

import com.kewen.framework.boot.authority.model.LoginReq;
import com.kewen.framework.boot.authority.model.LoginResp;

/**
 * @descrpition 登录Service
 * @author kewen
 * @since 2022-11-28 17:16
 */
public interface LoginService {
    LoginResp login(LoginReq loginReq);
}
