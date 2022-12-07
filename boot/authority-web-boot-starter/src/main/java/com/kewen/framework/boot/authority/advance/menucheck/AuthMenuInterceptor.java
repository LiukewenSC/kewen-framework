package com.kewen.framework.boot.authority.advance.menucheck;

import com.kewen.framework.base.authority.context.CurrentUserContext;
import com.kewen.framework.base.authority.service.SysMenuService;
import com.kewen.framework.base.common.exception.AuthorizationException;
import com.kewen.framework.boot.authority.service.SysMenuAuthUnify;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;

/**
 * @descrpition 菜单权限校验拦截器，校验请求在菜单中配置的权限，
 *      当Controller中加入注解 {@link AuthMenu} 时生效，否则直接跳过校验
 * @author kewen
 * @since 2022-11-25 14:39
 */
@Component
public class AuthMenuInterceptor implements HandlerInterceptor {

    @Autowired
    private SysMenuAuthUnify menuService;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)){
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        AuthMenu authMenu = handlerMethod.getMethodAnnotation(AuthMenu.class);
        if (authMenu ==null){
            return true;
        }
        String url= null;
        if (StringUtils.isBlank(authMenu.url())){
            url = request.getRequestURI();
        } else {
            url= authMenu.url();
        }
        Collection<String> userAuthorities = CurrentUserContext.getCurrentUserAuths();
        if (CollectionUtils.isEmpty(userAuthorities)){
            throw new AuthorizationException("验证失败，权限为空");
        }
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
