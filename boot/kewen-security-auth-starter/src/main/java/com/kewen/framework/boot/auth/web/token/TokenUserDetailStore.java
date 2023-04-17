package com.kewen.framework.boot.auth.web.token;


import com.kewen.framework.auth.core.model.AuthUserInfo;

/**
 * @descrpition 
 * @author kewen
 * @since 2022-11-29 11:21
 */
public interface TokenUserDetailStore {

    AuthUserInfo getAuthUserInfo(String token);

    void setUserDetail(String token, AuthUserInfo userDetail);
}
