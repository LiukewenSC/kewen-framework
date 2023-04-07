package com.kewen.framework.boot.auth.web.support.token;

import com.kewen.framework.base.authority.model.UserDetail;

/**
 * @descrpition 
 * @author kewen
 * @since 2022-11-29 11:21
 */
public interface TokenUserDetailStore {

    UserDetail getUserDetail(String token);

    void setUserDetail(String token, UserDetail userDetail);
}
