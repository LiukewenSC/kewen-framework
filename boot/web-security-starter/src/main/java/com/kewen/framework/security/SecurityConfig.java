package com.kewen.framework.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kewen.framework.base.common.model.Result;
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.PrintWriter;

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
     * 登录过滤器
     * @return
     * @throws Exception
     */
    @Bean
    RequestBodyUsernamePasswordAuthenticationProcessingFilter loginFilter() throws Exception {
        RequestBodyUsernamePasswordAuthenticationProcessingFilter processingFilter = new RequestBodyUsernamePasswordAuthenticationProcessingFilter();
        processingFilter.setAuthenticationManager(authenticationManager());
        processingFilter.setAuthenticationSuccessHandler((request, response, authentication) -> {
            response.setContentType("application/json;charset=utf-8");
            PrintWriter out = response.getWriter();
            Result result =Result.success(authentication);
            out.write(new ObjectMapper().writeValueAsString(result));
            out.flush();
            out.close();
        });
        processingFilter.setAuthenticationFailureHandler((request, response, exception) -> {
            response.setContentType("application/json;charset=utf-8");
            PrintWriter out = response.getWriter();
            Result result =Result.failed(exception.getMessage());
            out.write(new ObjectMapper().writeValueAsString(result));
            out.flush();
            out.close();
        });
        return processingFilter;
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
                    .anyRequest().authenticated().and()
                .addFilterAt(loginFilter(),UsernamePasswordAuthenticationFilter.class)
                .cors().disable()
                .csrf().disable()
                .exceptionHandling()
                .authenticationEntryPoint((request, response, e) -> {   //操作异常返回
                    log.error(" security error :"+e.getMessage(),e);
                    response.setContentType("application/json;charset=utf-8");
                    Result result =null;
                    PrintWriter out = response.getWriter();
                    if (e instanceof InsufficientAuthenticationException){
                        //result = Result.failed("请先进行认证");
                    }
                    if (result ==null){
                        result = Result.failed(e.getMessage());
                    }
                    out.write(new ObjectMapper().writeValueAsString(result));
                    out.flush();
                    out.close();
                }).accessDeniedHandler((request, response, e) -> {
                    log.error(" security error :"+e.getMessage(),e);
                    Result result ;
                    response.setContentType("application/json;charset=utf-8");
                    PrintWriter out = response.getWriter();
                    result = Result.failed(e.getMessage());
                    out.write(new ObjectMapper().writeValueAsString(result));
                    out.flush();
                    out.close();
                }).and()
                .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout","POST"))
                .logoutSuccessHandler((request, response, authentication) -> {
                    response.setContentType("application/json;charset=utf-8");
                    PrintWriter out = response.getWriter();
                    out.write(new ObjectMapper().writeValueAsString(Result.success("注销成功")));
                    out.flush();
                })
                .deleteCookies()   //清除Cookie
                .clearAuthentication(true)   // 清除 认证信息 默认就会清除。
                .invalidateHttpSession(true) // 清除 HttpSession，默认就会清除。
                .permitAll().and()
                .csrf().disable()
        ;

    }
}
