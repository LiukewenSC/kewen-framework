package com.kewen.framework.boot.auth.web;

import com.kewen.framework.boot.auth.AuthUserInfo;
import com.kewen.framework.boot.auth.context.AuthUserInfoContextContainer;

/**
 * @descrpition 用户上下文管理器，负责加载存储和获取上下文数据
 * @author kewen
 * @since 2022-11-25 17:06
 */
public interface WebAuthUserInfoContextContainer extends AuthUserInfoContextContainer {

    /**
     * 根据token设置用户信息，登录时需要将数据写入store中
     * @param userDetail 用户信息
     */
    String saveAuthUserInfo(AuthUserInfo userDetail);
}
