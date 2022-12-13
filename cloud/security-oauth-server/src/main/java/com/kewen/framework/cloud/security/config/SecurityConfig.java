package com.kewen.framework.cloud.security.config;

import com.kewen.framework.base.common.model.Result;
import com.kewen.framework.base.common.utils.BeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author kewen
 * @descrpition
 * @since 2022-12-07 15:58
 */
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    UserDetailsService userDetailsService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.formLogin()
                .usernameParameter("username")
                .passwordParameter("password")
                .loginProcessingUrl("/login")
                .failureHandler(
                        (httpServletRequest, httpServletResponse, e) -> {
                            httpServletResponse.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
                            httpServletResponse.getWriter().write(
                                    BeanUtil.toJsonString(
                                            Result.failed(e.getMessage())
                                    )
                            );
                        }
                )
                .successHandler(
                        (httpServletRequest, httpServletResponse, authentication) -> {
                            httpServletResponse.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
                            httpServletResponse.getWriter().write(
                                    BeanUtil.toJsonString(Result.success(authentication))
                            );
                        }
                )
        .and().csrf().disable()
                .cors().disable()
        ;
        //super.configure(http);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        super.configure(auth);
    }

    @Override
    protected UserDetailsService userDetailsService() {
        return userDetailsService;
    }
}
