package com.cu.framework.authentication.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.cu.framework.authentication.properties.SecurityProperties;
import com.cu.framework.authentication.response.AuthenticationSuccessResultResolver;
import com.cu.framework.authentication.response.AuthenticationSuccessResultConverter;
import com.cu.framework.authentication.response.SecurityAuthenticationExceptionResolverHandler;
import com.cu.framework.authentication.response.SecurityAuthenticationSuccessHandler;
import com.cu.framework.authentication.service.SecurityUserDetailsService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.session.SessionManagementFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author kewen
 * @descrpition
 * @since 2023-02-23
 */
@Configuration
@EnableConfigurationProperties({SecurityProperties.class})
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    SecurityProperties securityProperties;

    @Autowired
    SecurityUserDetailsService securityUserDetailsService;

    @Autowired
    SecurityAuthenticationSuccessHandler successHandler;

    @Autowired
    SecurityAuthenticationExceptionResolverHandler exceptionResolverHandler;

    @Autowired
    AuthenticationSuccessResultResolver resultResolverProvider ;

    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    ObjectProvider<AuthenticationSuccessResultConverter> jsonSuccessResultConverter;

    @Autowired
    ObjectProvider<HttpSecurityCustomizer> httpSecurityCustomizers;


    @Autowired
    ObjectProvider<UrlSecurityCustomizer> urlSecurityCustomizers;
    /**
     * 加入监听器，session销毁时才会触发 spring容器的感知，否则 security监听不到销毁
     * @return
     */
    @Bean
    HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    /**
     * 放行静态资源路径（/static、/js、/css、/image 及 html 页面），使其不经过安全过滤器链
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        //不拦截静态文件
        web.ignoring().antMatchers("/static/**", "/js/**", "/css/**", "/image/**", "/**/**.html");
    }

    /**
     * 向认证管理器注册 SecurityUserDetailsService，作为用户名密码认证的用户来源
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(securityUserDetailsService);
        super.configure(auth);
    }

    /**
     * 构建安全过滤链：汇总所有 UrlSecurityCustomizer 的放行 URL 后其余请求需认证；
     * 登出成功走 successHandler，认证入口与访问拒绝走 exceptionResolverHandler；禁用 csrf；
     * 在 SessionManagementFilter 之后注册 AuthUserContextFilter；
     * 最后依次应用容器中所有 HttpSecurityCustomizer（可覆盖以上配置）
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        List<String> permitList = new ArrayList<>();
        for (UrlSecurityCustomizer urlSecurityCustomizer : urlSecurityCustomizers) {
            permitList.addAll(urlSecurityCustomizer.permitAll());
        }
        http
                .authorizeRequests()
                    .antMatchers(permitList.toArray(new String[0])).permitAll()
                    .anyRequest().authenticated()
                    .and()
                //.formLogin()  不再用表单登录了，采用Json登录方式，因此不需要再formLogin引入FormLoginConfigurer配置UsernamePasswordAuthenticationFilter
                .logout()
                    .logoutSuccessHandler(successHandler)
                    .and()
                .exceptionHandling()
                    .accessDeniedHandler(exceptionResolverHandler)
                    .authenticationEntryPoint(exceptionResolverHandler)
                    .and()
                /*.sessionManagement()
                //.sessionFixation().changeSessionId()  //这里改为none则不会出现postman连续登录会报session过多 ， 要么就是允许挤下线
                    .sessionFixation().none()  //这里改为none则不会出现postman连续登录会报session过多
                        .maximumSessions(loginProperties.getMaximumSessions())
                        .maxSessionsPreventsLogin(loginProperties.getMaxSessionsPreventsLogin()).and()
                        .and()*/
                .csrf().disable()
                //.cors().configurationSource(corsConfigurationSource()).and()
        ;
        //如果有自定义配置就继续执行自定义的，会覆盖当前的配置
        Iterator<HttpSecurityCustomizer> iterator = httpSecurityCustomizers.stream().iterator();
        while (iterator.hasNext()) {
            HttpSecurityCustomizer httpSecurityCustomizer = iterator.next();
            httpSecurityCustomizer.customizer(http);
        }
    }

    /**
     * 跨域相关
     * @return
     */
    private CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.addAllowedOrigin("*");
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource corsConfigurationSource = new UrlBasedCorsConfigurationSource();
        corsConfigurationSource.registerCorsConfiguration("/**",corsConfiguration);
        return corsConfigurationSource;
    }

}
