package com.kewen.framework.boot.auth.annotation.endpoint;

import com.kewen.framework.boot.auth.context.CurrentUserContext;
import com.kewen.framework.base.authority.support.SysMenuAuthComposite;
import com.kewen.framework.base.common.exception.AuthorizationException;
import com.kewen.framework.boot.auth.annotation.CheckEndpoint;
import org.apache.commons.lang3.StringUtils;
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

    private final SysMenuAuthComposite menuService;

    public AuthMenuInterceptor(SysMenuAuthComposite menuService) {
        this.menuService = menuService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
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
        Collection<String> userAuthorities = CurrentUserContext.getCurrentUserAuths();
        boolean yjt = menuService.hasAuth(
                userAuthorities,
                url
        );
        if (!yjt){
            throw new AuthorizationException("验证失败");
        }
        return true;
    }
}