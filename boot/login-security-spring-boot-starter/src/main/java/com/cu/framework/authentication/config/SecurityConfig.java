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
     * 跨域（CORS）配置源：定义浏览器跨域访问本服务时的放行策略。
     * <p>
     * 注意：当前方法为 private 且未标注 {@code @Bean}，securityFilterChain() 中
     * {@code .cors(...)} 也被注释，因此该配置尚未生效。如需启用：
     * <ol>
     *     <li>在 securityFilterChain() 中打开 .cors(cors -> cors.configurationSource(...))；</li>
     *     <li>并将本方法改为 {@code @Bean}，交由 Spring Security 的 CorsFilter 使用。</li>
     * </ol>
     * 另外 allowCredentials(true) 与 allowedOrigin("*") 在 Spring 6 下会冲突，
     * 正式启用时应改用 setAllowedOriginPatterns(...) 或改为明确的来源白名单。
     *
     * @return 绑定到 /** 全路径的跨域配置源
     */
    private CorsConfigurationSource corsConfigurationSource(){
        // 声明一套跨域规则
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        // 允许跨域请求携带凭证（Cookie、Session、Authorization 等），前后端分离登录必需
        corsConfiguration.setAllowCredentials(true);
        // 允许任意来源的页面发起请求（注意：开启凭证后此处应改为 originPatterns）
        corsConfiguration.addAllowedOrigin("*");
        // 允许任意请求头（如 Content-Type、Authorization）
        corsConfiguration.addAllowedHeader("*");
        // 允许任意 HTTP 方法（GET/POST/PUT/DELETE/OPTIONS 等）
        corsConfiguration.addAllowedMethod("*");

        // 按 URL 路径注册跨域规则
        UrlBasedCorsConfigurationSource corsConfigurationSource = new UrlBasedCorsConfigurationSource();
        // 将上述规则应用到所有路径，使整个服务的接口都遵循这套跨域策略
        corsConfigurationSource.registerCorsConfiguration("/**",corsConfiguration);
        return corsConfigurationSource;
    }

}
