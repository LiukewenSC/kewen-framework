package com.kewen.framework.boot.auth.web.support.session;

import com.kewen.framework.boot.auth.bussiness.model.LoginResp;
import com.kewen.framework.boot.auth.web.support.AbstractCurrentUserContextManager;
import com.kewen.framework.base.authority.model.UserDetail;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @descrpition 基于session的用户上下文
 * @author kewen
 * @since 2022-11-25 15:33
 */
public class SessionCurrentUserContextManager extends AbstractCurrentUserContextManager {

    public static final String USER_ATTR="currentUser";

    private final HttpSession httpSession;

    public SessionCurrentUserContextManager(HttpSession httpSession) {
        this.httpSession = httpSession;
    }

    @Override
    protected UserDetail fetchUserDetail(HttpServletRequest request) {
        HttpSession session = request.getSession();
        Object currentUser = session.getAttribute(USER_ATTR);
        return (UserDetail)currentUser;
    }


    @Override
    public UserDetail getUserDetail(String token) {
        Object attribute = httpSession.getAttribute(USER_ATTR);
        return (UserDetail) attribute;
    }

    @Override
    public LoginResp saveUserDetail(UserDetail userDetail) {
        httpSession.setAttribute(USER_ATTR,userDetail);
        LoginResp copy = LoginResp.copy(userDetail);
        return copy;
    }
}