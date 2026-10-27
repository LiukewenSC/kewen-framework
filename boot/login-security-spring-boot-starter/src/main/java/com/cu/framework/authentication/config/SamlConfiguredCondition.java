package com.cu.framework.authentication.config;

import com.cu.framework.authentication.saml.properties.SamlProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Collections;

/**
 * SAML2 登录装配条件：{@code cu.security.login.saml.registrations} 至少配置一条时才装配。
 * 纯账号密码登录的部署不配置 registrations，此时不注册 SAML 登录过滤器，也不要求 IdP 元数据文件。
 *
 * @author kewen
 * @since 2026-10-27
 */
public class SamlConfiguredCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return !Binder.get(context.getEnvironment())
                .bind("cu.security.login.saml.registrations",
                        Bindable.listOf(SamlProperties.RegistrationSamlProperties.class))
                .orElse(Collections.emptyList())
                .isEmpty();
    }
}
