package com.kewen.framework.auth.core.annotation.endpoint;

import com.kewen.framework.auth.core.AuthHandler;
import com.kewen.framework.auth.core.annotation.CheckEndpoint;
import com.kewen.framework.common.core.exception.AuthorizationException;
import com.kewen.framework.auth.core.context.AuthUserContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;

/**
 * @descrpition 菜单权限校验拦截器，校验请求在菜单中配置的权限，
 *      当Controller中加入注解 {@link CheckEndpoint} 时生效，否则直接跳过校验
 * @author kewen
 * @since 2022-11-25 14:39
 */
public class AuthMenuInterceptor implements HandlerInterceptor {

    @Autowired
    private AuthHandler authHandler;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)){
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        CheckEndpoint checkEndpoint = handlerMethod.getMethodAnnotation(CheckEndpoint.class);
        if (checkEndpoint ==null){
            return true;
        }
        String url= null;
        if (StringUtils.isBlank(checkEndpoint.url())){
            url = request.getRequestURI();
        } else {
            url= checkEndpoint.url();
        }
        Collection<String> userAuthorities = AuthUserContext.getAuthorities();
        boolean yjt = authHandler.hasMenuAuth(
                userAuthorities,
                url
        );
        if (!yjt){
            throw new AuthorizationException("验证失败");
        }
        return true;
    }
}
