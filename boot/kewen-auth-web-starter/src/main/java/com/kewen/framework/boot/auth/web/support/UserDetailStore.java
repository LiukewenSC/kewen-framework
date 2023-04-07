package com.kewen.framework.boot.auth.web.support;

import com.kewen.framework.boot.auth.bussiness.model.LoginResp;
import com.kewen.framework.base.authority.model.UserDetail;

/**
 * @descrpition  存储用户token对应的UserDetail实例的
 * @author kewen
 * @since 2022-11-25 15:49
 */
public interface UserDetailStore {

    /**
     * 根据token获取用户信息
     * @param token
     * @return
     */
    UserDetail getUserDetail(String token);

    /**
     * 根据token设置用户信息，登录时需要将数据写入store中
     * @param userDetail 用户信息
     */
    LoginResp saveUserDetail(UserDetail userDetail);
}