package com.kewen.framework.cloud.security.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
/**
 * @author kewen
 * @descrpition
 * @since 2022-12-07 15:58
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    UserDetailsService userDetailsService;

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/login.html", "/css/**", "/js/**", "/images/**");
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser("sang")
                .password(new BCryptPasswordEncoder().encode("123"))
                .roles("admin")
                .authorities("s1","s2")
                .and()
                .withUser("javaboy")
                .password(new BCryptPasswordEncoder().encode("123"))
                .roles("user");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .requestMatchers()
                .antMatchers("/login").antMatchers("/oauth/authorize").and()
                .authorizeRequests().anyRequest().authenticated().and()
                //.formLogin().loginProcessingUrl("/login").loginPage("/login.html").and()
                .formLogin().and().csrf().disable();
    }
}
