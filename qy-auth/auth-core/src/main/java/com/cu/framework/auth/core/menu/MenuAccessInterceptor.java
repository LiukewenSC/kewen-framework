package com.cu.framework.auth.core.menu;

import com.cu.framework.auth.core.AuthCurrentUserContextHolder;
import com.cu.framework.auth.core.AuthMenu;
import com.cu.framework.auth.core.entity.BaseAuth;
import com.cu.framework.auth.core.exception.AuthException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;

/**
 * @descrpition 菜单权限校验拦截器，校验请求在菜单中配置的权限，
 *      当Controller中加入注解 {@link AuthMenu} 时生效，否则直接跳过校验
 * @author kewen
 * @since 2022-11-25
 */
public class MenuAccessInterceptor implements HandlerInterceptor {

    /** 菜单权限处理器，校验当前用户是否有菜单路径的访问权限 */
    private final AuthMenuHandler authMenuHandler;
    private final AuthCurrentUserContextHolder authCurrentUserContextHolder;

    public MenuAccessInterceptor(AuthMenuHandler authMenuHandler,
                                 AuthCurrentUserContextHolder authCurrentUserContextHolder) {
        this.authMenuHandler = authMenuHandler;
        this.authCurrentUserContextHolder = authCurrentUserContextHolder;
    }

    /**
     * 请求进入 Controller 方法前校验菜单权限：
     * 非 HandlerMethod 请求、或方法与所在类均未标注 {@link AuthMenu} 时直接放行；
     * 否则取去掉 contextPath 的请求路径与当前登录用户权限做校验
     * @return 是否放行，校验通过返回 true
     * @throws AuthException 当前用户没有该菜单路径访问权限时抛出
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        //不兼容的方法，即不是（Controller对应的方法）不做拦截
        if (!(handler instanceof HandlerMethod)){
            return true;
        }

        //是否有管辖声明：方法上没有就看所在类，都没有则跳过校验
        if (!isGoverned((HandlerMethod) handler)){
            return true;
        }
        //校验url
        String url = getUrl(request);


        //获取当前用户的权限
        Collection<BaseAuth> auths = authCurrentUserContextHolder.getAuths();

        //检查是否有菜单访问权限
        boolean hasMenuAuth = authMenuHandler.hasMenuAccessAuth(
                auths,
                url
        );
        if (!hasMenuAuth){
            throw new AuthException("没有API菜单"+url+"访问权限");
        }
        return true;
    }

    /**
     * 该端点是否受菜单管辖：方法上有 {@link AuthMenu} 声明即管辖该端点，
     * 方法没有则看所在类，类上声明即管辖该类的全部端点
     * @param handlerMethod 命中的 Controller 方法
     * @return 是否受管辖
     */
    static boolean isGoverned(HandlerMethod handlerMethod) {
        return handlerMethod.hasMethodAnnotation(AuthMenu.class)
                || handlerMethod.getMethod().getDeclaringClass().isAnnotationPresent(AuthMenu.class);
    }
    /**
     * 获取用于权限校验的请求路径，即 RequestURI 去掉 contextPath 前缀
     * @param request 当前请求
     * @return 应用内路径
     */
    private String getUrl(HttpServletRequest request){
        String url = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (StringUtils.isBlank(contextPath)){
            return url;
        } else {
            return url.replaceFirst(contextPath, "");
        }
    }
}
