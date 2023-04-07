package com.kewen.framework.boot.auth.web.support;

import com.kewen.framework.base.common.model.IUser;
import com.kewen.framework.base.authority.model.UserDetail;
import com.kewen.framework.boot.common.context.UserContextContainer;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @descrpition 用户上下文管理器，负责加载存储和获取上下文数据
 * @author kewen
 * @since 2022-11-25 17:06
 */
public abstract class AbstractCurrentUserContextManager implements HandlerInterceptor,UserDetailStore, UserContextContainer {

    private ThreadLocal<UserDetail> userContainer = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserDetail currentUser = fetchUserDetail(request);
        if (currentUser !=null){
            userContainer.set(currentUser);
        }
        return true;
    }

    @Nullable
    protected abstract UserDetail fetchUserDetail(HttpServletRequest request);

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        userContainer.remove();
    }

    @Override
    public IUser getUser() {
        return userContainer.get();
    }
}
