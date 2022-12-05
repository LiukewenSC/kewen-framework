package com.kewen.framework.boot.authority.biz.service;

import com.kewen.framework.boot.authority.biz.model.req.LoginReq;
import com.kewen.framework.boot.authority.biz.model.resp.LoginResp;

/**
 * @descrpition 登录Service
 * @author kewen
 * @since 2022-11-28 17:16
 */
public interface LoginService {
    LoginResp login(LoginReq loginReq);
}
