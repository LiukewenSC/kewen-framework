package com.kewen.framework.boot.authority.currentuser;

import com.kewen.framework.base.authority.context.CurrentUserContext;
import com.kewen.framework.base.common.model.UserDetail;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @descrpition 用户上下文设置拦截器
 * @author kewen
 * @since 2022-11-25 17:06
 */
public abstract class AbstractCurrentUserContextManager implements HandlerInterceptor,UserDetailStore {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        UserDetail currentUser = fetchUserDetail(request);
        if (currentUser !=null){
            CurrentUserContext.setCurrentUser(currentUser);
        }
        return true;
    }

    @Nullable
    protected abstract UserDetail fetchUserDetail(HttpServletRequest request);

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        CurrentUserContext.clearCurrentUser();
    }

}
