package com.cu.framework.auth.security.password.config;

import com.cu.framework.auth.security.password.filter.JsonLoginFilter;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * @author kewen
 * @descrpition json登录的配置 , 继承AbstractAuthenticationFilterConfigurer 完成自定义的登录过滤器的配置
 * @since 2023-03-01
 */
public class JsonLoginAuthenticationFilterConfigurer<B extends HttpSecurityBuilder<B>>
        extends AbstractAuthenticationFilterConfigurer<B, JsonLoginAuthenticationFilterConfigurer<B>, JsonLoginFilter> {

    /**
     * 创建基于 JsonLoginFilter 的配置器，loginPage 传 null（JSON 登录无登录页）
     */
    public JsonLoginAuthenticationFilterConfigurer() {
        super(new JsonLoginFilter(), null);
    }

    /**
     * 登录处理 URL 以 POST 方式的 Ant 路径匹配器进行匹配
     * @return 匹配器实例
     */
    @Override
    protected RequestMatcher createLoginProcessingUrlMatcher(String loginProcessingUrl) {
        return new AntPathRequestMatcher(loginProcessingUrl, "POST");
    }

    /**
     * JSON 登录不支持登录页配置，调用即抛出异常使启动失败
     * @throws RuntimeException 始终抛出
     */
    @Override
    protected JsonLoginAuthenticationFilterConfigurer<B> loginPage(String loginPage) {
        throw new RuntimeException("启动失败，不支持配置loginPage");
    }
    /**
     * 设置登录 JSON 中用户名的字段名
     * @param usernameParameter 用户名字段名
     * @return 自身，支持链式调用
     */
    public JsonLoginAuthenticationFilterConfigurer<B> usernameParameter(String usernameParameter) {
        getAuthenticationFilter().setUsernameParameter(usernameParameter);
        return this;
    }
    /**
     * 设置登录 JSON 中密码的字段名
     * @param passwordParameter 密码字段名
     * @return 自身，支持链式调用
     */
    public JsonLoginAuthenticationFilterConfigurer<B> passwordParameter(String passwordParameter) {
        getAuthenticationFilter().setPasswordParameter(passwordParameter);
        return this;
    }
}
