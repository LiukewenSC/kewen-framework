package com.kewen.framework.boot.auth.web.service;

import com.kewen.framework.base.authority.model.resp.LoginReq;
import com.kewen.framework.base.authority.model.resp.LoginResp;

/**
 * @descrpition 登录Service
 * @author kewen
 * @since 2022-11-28 17:16
 */
public interface LoginService {
    LoginResp login(LoginReq loginReq);
}
