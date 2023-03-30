package com.kewen.framework.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kewen.framework.base.common.model.Result;
import com.kewen.framework.security.support.JsonLoginAuthenticationFilterConfigurer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.rememberme.InMemoryTokenRepositoryImpl;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

/**
 * @author kewen
 * @descrpition 安全配置
 * @since 2023-02-28
 */
@Configuration
@Slf4j
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Bean
    public PasswordEncoder passwordEncoder(){
        return NoOpPasswordEncoder.getInstance();
    }

    /**
     * 加入监听器，session销毁时才会触发 spring容器的感知，否则 security监听不到销毁
     * @return
     */
    @Bean
    HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication().withUser("user").password("123456").authorities("R_0","R_1");
    }

    @Bean
    @Override
    protected AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        super.configure(web);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
                .authorizeRequests().antMatchers(HttpMethod.GET,"/login").permitAll()
                    .anyRequest().authenticated()
                    .and()
                //.addFilterAt(loginFilter(),UsernamePasswordAuthenticationFilter.class)
                //.formLogin().and()
                .apply(new JsonLoginAuthenticationFilterConfigurer<>())  //采用新建配置类的方式可以使得原来config中配置的对象依然有效
                    .loginProcessingUrl("/login")
                    .usernameParameter("username")
                    .passwordParameter("password")
                    .successHandler((request, response, authentication) -> {
                        response.setContentType("application/json;charset=utf-8");
                        PrintWriter out = response.getWriter();
                        Result result =Result.success(authentication);
                        writeResponseBody(response,result);
                    })
                    .failureHandler((request, response, exception) -> {
                        response.setContentType("application/json;charset=utf-8");
                        PrintWriter out = response.getWriter();
                        Result result =Result.failed(exception.getMessage());
                        writeResponseBody(response,result);
                    })
                    .and()
                .exceptionHandling()
                    .authenticationEntryPoint((request, response, e) -> {   //操作异常返回
                        log.error(" security error :"+e.getMessage(),e);
                        Result result =null;
                        if (e instanceof InsufficientAuthenticationException){
                            //result = Result.failed("请先进行认证");
                        }
                        if (result ==null){
                            result = Result.failed(e.getMessage());
                        }
                        writeResponseBody(response,result);
                    }).accessDeniedHandler((request, response, e) -> {
                        log.error(" security error :"+e.getMessage(),e);
                        Result result ;
                        result = Result.failed(e.getMessage());
                        writeResponseBody(response,result);
                    }).and()
                .sessionManagement()
                    //.sessionAuthenticationStrategy(sessionAuthenticationStrategy())
                    .maximumSessions(1)
                        //.sessionRegistry(sessionRegistry())
                        .maxSessionsPreventsLogin(true)
                        .expiredSessionStrategy(event -> {
                            HttpServletResponse response = event.getResponse();
                            writeResponseBody(response,Result.failed("session已过期"));
                        }).and()
                    .and()
                .rememberMe()
                    .useSecureCookie(true)
                    .key("rememberMe")
                    .tokenRepository(new InMemoryTokenRepositoryImpl())  //此处可以替换成基于数据库的，不加默认用户数据都在cookie中，（类似于jwt，是不是就可以实现分布式了呢？）
                    .tokenValiditySeconds(2*7*24*60*60) //默认保存两周时间
                    .and()
                .logout()
                    .logoutRequestMatcher(new AntPathRequestMatcher("/logout","POST"))
                    .logoutSuccessHandler((request, response, authentication) -> {
                        writeResponseBody(response,Result.success("注销成功"));
                    })
                    .deleteCookies()   //清除Cookie
                    .clearAuthentication(true)   // 清除 认证信息 默认就会清除。
                    .invalidateHttpSession(true) // 清除 HttpSession，默认就会清除。
                    .permitAll()
                    .and()
                .csrf().disable()
        ;


    }

    private void writeResponseBody(HttpServletResponse response,Result result) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        PrintWriter out = response.getWriter();
        out.write(new ObjectMapper().writeValueAsString(result));
        out.flush();
    }
}