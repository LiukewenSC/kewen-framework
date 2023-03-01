package com.kewen.framework.security.support;

import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * @author kewen
 * @descrpition json登录的配置 , 继承AbstractAuthenticationFilterConfigurer 完成自定义的登录过滤器的配置
 * @since 2023-03-01
 */
public class JsonLoginAuthenticationFilterConfigurer<H extends HttpSecurityBuilder<H>> extends AbstractAuthenticationFilterConfigurer<H, JsonLoginAuthenticationFilterConfigurer<H>, RequestBodyUsernamePasswordAuthenticationFilter> {

    public JsonLoginAuthenticationFilterConfigurer() {
        super(new RequestBodyUsernamePasswordAuthenticationFilter(), "/login");
    }

    @Override
    protected RequestMatcher createLoginProcessingUrlMatcher(String loginProcessingUrl) {
        return new AntPathRequestMatcher(loginProcessingUrl, "POST");
    }

    @Override
    protected JsonLoginAuthenticationFilterConfigurer<H> loginPage(String loginPage) {
        throw new RuntimeException("启动失败，不支持配置loginPage");
    }
}
