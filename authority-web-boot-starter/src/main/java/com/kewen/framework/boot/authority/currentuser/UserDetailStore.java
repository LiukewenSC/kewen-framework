package com.kewen.framework.boot.authority.currentuser;

import com.kewen.framework.boot.authority.biz.model.resp.LoginResp;
import com.kewen.common.model.UserDetail;

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
