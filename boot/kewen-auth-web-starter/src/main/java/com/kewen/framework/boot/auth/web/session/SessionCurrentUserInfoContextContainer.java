package com.kewen.framework.boot.auth.web.session;

import com.kewen.framework.boot.auth.AuthUserInfo;
import com.kewen.framework.boot.auth.web.WebAuthUserInfoContextContainer;

import javax.servlet.http.HttpSession;

/**
 * @descrpition 基于session的用户上下文
 * @author kewen
 * @since 2022-11-25 15:33
 */
public class SessionCurrentUserInfoContextContainer implements WebAuthUserInfoContextContainer {

    public static final String USER_ATTR="currentUser";

    private final HttpSession httpSession;

    public SessionCurrentUserInfoContextContainer(HttpSession httpSession) {
        this.httpSession = httpSession;
    }


    @Override
    public AuthUserInfo getAuthUserInfo() {
        Object attribute = httpSession.getAttribute(USER_ATTR);
        return (AuthUserInfo) attribute;
    }

    @Override
    public String saveAuthUserInfo(AuthUserInfo userDetail) {
        httpSession.setAttribute(USER_ATTR,userDetail);
        return null;
    }
}
