package com.cu.framework.authentication.config;

import com.cu.framework.authentication.properties.SecurityProperties;
import com.cu.framework.authentication.response.SecurityAuthenticationExceptionResolverHandler;
import com.cu.framework.authentication.response.SecurityAuthenticationSuccessHandler;
import com.cu.framework.authentication.service.SecurityUserDetailsService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kewen
 * @descrpition
 * @since 2023-02-23
 */
@AutoConfiguration
@EnableConfigurationProperties({SecurityProperties.class})
public class SecurityConfig {

    @Autowired
    SecurityAuthenticationSuccessHandler successHandler;

    @Autowired
    SecurityAuthenticationExceptionResolverHandler exceptionResolverHandler;

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
     * 以容器中的 SecurityUserDetailsService 与 PasswordEncoder 组装认证提供者，
     * AuthenticationManager 由 Spring Security 基于该 Provider 统一装配
     */
    @Bean
    DaoAuthenticationProvider daoAuthenticationProvider(SecurityUserDetailsService securityUserDetailsService,
                                                        PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(securityUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    /**
     * 放行静态资源路径（/static、/js、/css、/image 及 html 页面），使其不经过安全过滤器链
     */
    @Bean
    WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring()
                .requestMatchers("/static/**", "/js/**", "/css/**", "/image/**", "/**/*.html");
    }

    /**
     * 构建安全过滤链：汇总所有 UrlSecurityCustomizer 的放行 URL 后其余请求需认证；
     * 登出成功走 successHandler，认证入口与访问拒绝走 exceptionResolverHandler；禁用 csrf；
     * 最后依次应用容器中所有 HttpSecurityCustomizer（可覆盖以上配置）
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        List<String> permitList = new ArrayList<>();
        for (UrlSecurityCustomizer urlSecurityCustomizer : urlSecurityCustomizers) {
            permitList.addAll(urlSecurityCustomizer.permitAll());
        }
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(permitList.toArray(new String[0])).permitAll()
                        .anyRequest().authenticated())
                .logout(logout -> logout.logoutSuccessHandler(successHandler))
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .accessDeniedHandler(exceptionResolverHandler)
                        .authenticationEntryPoint(exceptionResolverHandler))
                //见 {@link SessionConfig#httpSecurityCustomizer}
                /*.sessionManagement(session -> session
                        .sessionFixation().changeSessionId()  //这里改为none则不会出现postman连续登录会报session过多 ， 要么就是允许挤下线
                        .sessionFixation().none()  //这里改为none则不会出现postman连续登录会报session过多
                        .maximumSessions(securityProperties.getMaximumSessions())
                        .maxSessionsPreventsLogin(securityProperties.getMaxSessionsPreventsLogin())

                )*/
                .csrf(AbstractHttpConfigurer::disable)
                //.cors(cors -> cors.configurationSource(corsConfigurationSource()))
        ;
        //如果有自定义配置就继续执行自定义的，会覆盖当前的配置
        for (HttpSecurityCustomizer httpSecurityCustomizer : httpSecurityCustomizers) {
            httpSecurityCustomizer.customizer(http);
        }
        return http.build();
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
