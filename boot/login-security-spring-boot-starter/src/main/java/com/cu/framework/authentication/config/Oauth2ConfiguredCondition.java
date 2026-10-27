package com.cu.framework.authentication.config;

import com.cu.framework.authentication.oauth2.Oauth2OidcProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Collections;

/**
 * OAuth2/OIDC 登录装配条件：{@code cu.security.login.oauth2.clients} 至少配置一条时才装配。
 * 纯账号密码登录的部署不配置 clients，此时不注册 OAuth2 登录过滤器，也不要求占位客户端。
 *
 * @author kewen
 * @since 2026-10-27
 */
public class Oauth2ConfiguredCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return !Binder.get(context.getEnvironment())
                .bind("cu.security.login.oauth2.clients",
                        Bindable.listOf(Oauth2OidcProperties.Oauth2OidcRegistrationClientProperties.class))
                .orElse(Collections.emptyList())
                .isEmpty();
    }
}
