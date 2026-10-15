package com.cu.framework.authentication;

import com.cu.framework.authentication.model.SecurityUser;
import com.cu.framework.authentication.response.AuthenticationSuccessResultConverter;
import com.cu.framework.authentication.response.AuthenticationSuccessResultResolver;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

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

    public static SecurityUser getCurrentUser() {
        Optional<Authentication> optionalAuthentication = Optional.of(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication);
        //设置用户权限上下文
        if (optionalAuthentication.isPresent()){
            Authentication authentication = optionalAuthentication.get();
            for (AuthenticationSuccessResultConverter successResultConverter : jsonSuccessResultConverter) {
                if (successResultConverter.support(authentication)) {
                    return successResultConverter.convert(userDetailsService, authentication);
                }
            }
        }
        throw new RuntimeException("当前用户不存在");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        jsonSuccessResultConverter = applicationContext.getBeanProvider(AuthenticationSuccessResultConverter.class);
        userDetailsService = applicationContext.getBean(UserDetailsService.class);
    }
}
