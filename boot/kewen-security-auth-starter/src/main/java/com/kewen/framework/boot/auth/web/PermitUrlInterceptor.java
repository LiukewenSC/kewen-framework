package com.kewen.framework.boot.auth.web;

import com.kewen.framework.auth.context.AuthUserContext;
import com.kewen.framework.auth.core.model.AuthUserInfo;
import com.kewen.framework.boot.auth.AuthProperties;
import com.kewen.framework.boot.auth.PermitUrlContainer;
import com.kewen.framework.common.core.exception.AuthorizationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

/**
 * 用户上下文管理器，负责加载存储和获取上下文数据
 *
 * @author kewen
 * @since 2023-06-15
 */
public class PermitUrlInterceptor implements HandlerInterceptor {


    AuthProperties authProperties;

    PermitUrlContainer permitUrlContainer;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String uri = request.getRequestURI();

        String[] permitUrls = permitUrlContainer.getPermitUrls();
        String loginEndpoint = authProperties.getLoginEndpoint();
        String logoutEndpoint = authProperties.getLogoutEndpoint();
        if (uri.equals(loginEndpoint) || uri.equals(logoutEndpoint) || Arrays.asList(permitUrls).contains(uri)) {
            AuthUserInfo<?> authUserInfo = AuthUserContext.get();
            if (authUserInfo == null) {
                throw new AuthorizationException("访问的资源需要认证");
            }
        }
        return true;
    }

    public void setAuthProperties(AuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    public void setPermitUrlContainer(PermitUrlContainer permitUrlContainer) {
        this.permitUrlContainer = permitUrlContainer;
    }
}
