package com.kewen.framework.boot.authority.config;

import com.kewen.framework.base.authority.service.SysApplicationAuthService;
import com.kewen.framework.base.authority.service.SysMenuService;
import com.kewen.framework.base.authority.service.impl.SysApplicationAuthServiceImpl;
import com.kewen.framework.base.authority.service.impl.SysMenuServiceImpl;
import com.kewen.framework.boot.authority.advance.menucheck.AuthMenuInterceptor;
import com.kewen.framework.boot.authority.currentuser.AbstractCurrentUserContextManager;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author kewen
 * @descrpition
 * @since 2022-11-23 9:20
 */
@EnableAspectJAutoProxy
@Configuration
@ComponentScan("com.kewen")
@MapperScan("com.kewen.**.mapper")
public class WebAuthorityConfig implements WebMvcConfigurer {

    @Autowired
    private AuthMenuInterceptor authMenuInterceptor;
    @Autowired
    AbstractCurrentUserContextManager currentUserContextInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        //用户当前用户拦截器，此拦截器一定要在最前面，保证当前用户设置到上下文中
        registry.addInterceptor(currentUserContextInterceptor).excludePathPatterns("/login/**");

        //此拦截器放在当前用户拦截器之后，里面内容会先从用户上下文中获取数据
        registry.addInterceptor(authMenuInterceptor);

    }




    /**
     * 菜单Service，当菜单的表不是sys_menu时可以自行实现
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(SysMenuService.class)
    public SysMenuService sysMenuService(){
        return new SysMenuServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean(SysApplicationAuthService.class)
    public SysApplicationAuthService sysApplicationAuthService(){
        return new SysApplicationAuthServiceImpl();
    }



}
