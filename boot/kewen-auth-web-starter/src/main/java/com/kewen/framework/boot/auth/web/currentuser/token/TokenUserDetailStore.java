package com.kewen.framework.boot.auth.web.currentuser.token;

import com.kewen.framework.base.common.model.UserDetail;

/**
 * @descrpition 
 * @author kewen
 * @since 2022-11-29 11:21
 */
public interface TokenUserDetailStore {

    UserDetail getUserDetail(String token);

    void setUserDetail(String token, UserDetail userDetail);
}
