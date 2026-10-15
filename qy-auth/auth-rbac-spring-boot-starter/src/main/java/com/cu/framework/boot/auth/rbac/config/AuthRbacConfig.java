package com.cu.framework.boot.auth.rbac.config;

import com.cu.framework.auth.core.AuthDataAdaptor;
import com.cu.framework.auth.core.data.JdbcAuthDataPersistent;
import com.cu.framework.auth.core.menu.AuthMenuHandler;
import com.cu.framework.auth.rbac.RbacAnnotationAuthHandler;
import com.cu.framework.auth.rbac.composite.AuthMenuStore;
import com.cu.framework.auth.rbac.composite.SysAuthMenuComposite;
import com.cu.framework.auth.rbac.composite.SysUserComposite;
import com.cu.framework.auth.rbac.composite.impl.MemoryExpiredAuthMenuStore;
import com.cu.framework.auth.rbac.extension.RbacCurrentUserService;
import com.cu.framework.auth.rbac.extension.RbacMenuApiService;
import com.cu.framework.auth.rbac.mp.service.SysMenuApiMpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 *  impl 实现相关
 *  有了子包才有此处配置
 * @author kewen
 * @since 2023-12-28
 */
@Configuration
public class AuthRbacConfig {

    @Autowired
    SysMenuApiMpService sysMenuApiMpService;
    @Autowired
    JdbcAuthDataPersistent jdbcAuthDataPersistent;
    /**
     * 注解权限处理器
     * 在引入了<cu-auth-impl> 依赖的情况下配置
     * 在系统中没有自定义AnnotationAuthHandler时配置
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(AuthMenuHandler.class)
    public RbacAnnotationAuthHandler annotationAuthHandler(SysAuthMenuComposite sysAuthMenuComposite){
        RbacAnnotationAuthHandler handler = new RbacAnnotationAuthHandler();
        handler.setSysAuthMenuComposite(sysAuthMenuComposite);
        return handler;
    }

    /**
     * 调用服务，可以从这里调用，就不再需要从注解 @AuthDataAuthEdit上执行了，提供了第二种可能
     * @return
     */
    @Bean
    public AuthDataAdaptor authDateAdaptor(){
        return new AuthDataAdaptor();
    }

    @Bean
    RbacCurrentUserService rbacCurrentUserService(
            SysUserComposite sysUserComposite, PasswordEncoder passwordEncoder){

        RbacCurrentUserService service = new RbacCurrentUserService();
        service.setSysUserComposite(sysUserComposite);
        service.setPasswordEncoder(passwordEncoder);
        return service;

    }
    /**
     * 菜单API服务
     * @return
     */
    @Bean
    RbacMenuApiService rbacMenuApiStore(){
        RbacMenuApiService store = new RbacMenuApiService();
        store.setSysMenuApiMpService(sysMenuApiMpService);
        return store;
    }

    /**
     * 菜单存储，可以基于内存或redis实现
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(AuthMenuStore.class)
    AuthMenuStore authMenuStore(){
        return new MemoryExpiredAuthMenuStore();
    }

    @Bean
    RbacResultAdviceHandler rbacResultAdviceHandler(){
        return new RbacResultAdviceHandler();
    }
}
