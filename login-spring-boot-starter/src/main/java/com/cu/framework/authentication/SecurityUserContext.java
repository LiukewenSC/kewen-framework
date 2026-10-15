package com.cu.framework.authentication;

import com.cu.framework.authentication.exception.NoLoginException;
import com.cu.framework.authentication.model.SecurityUser;
import com.cu.framework.authentication.response.AuthenticationSuccessResultConverter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * 用户上下文
 * @author kewen
 * @since 2026-10-15
 */
public class SecurityUserContext implements ApplicationContextAware {

    /** Authentication 转 SecurityUser 的转换器提供者 */
    private static ObjectProvider<AuthenticationSuccessResultConverter> jsonSuccessResultConverter;
    /** 用户详情服务，供转换器加载用户使用 */
    private static UserDetailsService userDetailsService;

    /**
     * 用支持的转换器把当前认证信息转为登录用户
     * @return 登录用户，未登录（含匿名）或没有支持的转换器时返回 null
     */
    public static SecurityUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new NoLoginException("未登录");
        }
        for (AuthenticationSuccessResultConverter successResultConverter : jsonSuccessResultConverter) {
            if (successResultConverter.support(authentication)) {
                return successResultConverter.convert(userDetailsService, authentication);
            }
        }
        throw new NoLoginException("未转换成功用户详情");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        jsonSuccessResultConverter = applicationContext.getBeanProvider(AuthenticationSuccessResultConverter.class);
        userDetailsService = applicationContext.getBean(UserDetailsService.class);
    }
}
