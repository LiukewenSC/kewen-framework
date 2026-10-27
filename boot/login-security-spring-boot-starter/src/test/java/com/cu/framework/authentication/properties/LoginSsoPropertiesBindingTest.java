package com.cu.framework.authentication.properties;

import com.cu.framework.authentication.saml.properties.SamlProperties;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * 单点登录结果页地址与 SAML 展示名两个新增配置项的绑定与回退行为
 *
 * @author kewen
 * @since 2026-10-16
 */
public class LoginSsoPropertiesBindingTest {

    private Binder binderOf(Map<String, String> values) {
        ConfigurationPropertySource source = new MapConfigurationPropertySource(values);
        return new Binder(source);
    }

    /**
     * kebab-case 的 result-url 应绑定到驼峰字段，且留空时字段为 null（不产生空串假象）
     */
    @Test
    public void resultUrlBindsFromKebabCaseName() {
        Map<String, String> values = new HashMap<>();
        values.put("cu.security.login.sso.result-url", "/login.html");
        SsoLoginProperties properties = binderOf(values)
                .bind("cu.security.login.sso", Bindable.of(SsoLoginProperties.class))
                .get();
        Assert.assertEquals("/login.html", properties.getResultUrl());

        SsoLoginProperties empty = binderOf(new HashMap<>())
                .bind("cu.security.login.sso", Bindable.of(SsoLoginProperties.class))
                .orElseGet(SsoLoginProperties::new);
        Assert.assertNull("未配置时应保持 null，交付姿态据此回退为 JSON", empty.getResultUrl());
    }

    /**
     * registrations[].display-name 应绑定到 RegistrationSamlProperties.displayName
     */
    @Test
    public void samlDisplayNameBinds() {
        Map<String, String> values = new HashMap<>();
        values.put("cu.security.login.saml.registrations[0].registration-id", "demo-saml");
        values.put("cu.security.login.saml.registrations[0].display-name", "企业单点登录");
        SamlProperties properties = binderOf(values)
                .bind("cu.security.login.saml", Bindable.of(SamlProperties.class))
                .get();
        Assert.assertEquals("企业单点登录",
                properties.getRegistrations().get(0).resolveDisplayName());
    }

    /**
     * 展示名缺省时回退 registrationId；registrationId 也缺省时继续回退 spEntityId
     */
    @Test
    public void displayNameFallsBackToRegistrationIdThenSpEntityId() {
        SamlProperties.RegistrationSamlProperties withRegistrationId = new SamlProperties.RegistrationSamlProperties();
        withRegistrationId.setRegistrationId("demo-saml");
        Assert.assertEquals("demo-saml", withRegistrationId.resolveDisplayName());

        SamlProperties.RegistrationSamlProperties onlyEntityId = new SamlProperties.RegistrationSamlProperties();
        onlyEntityId.setSpEntityId("cu-framework-local");
        Assert.assertEquals("cu-framework-local", onlyEntityId.resolveDisplayName());

        SamlProperties.RegistrationSamlProperties blankName = new SamlProperties.RegistrationSamlProperties();
        blankName.setRegistrationId("demo-saml");
        blankName.setDisplayName("   ");
        Assert.assertEquals("空白展示名不应被当成有效文案", "demo-saml", blankName.resolveDisplayName());
    }

    /**
     * 两项都没配的注册标识：展示名与注册标识同样取不到值，供发现接口剔除该条目的判据使用
     */
    @Test
    public void missingRegistrationIdAndSpEntityIdYieldsNullIdentity() {
        SamlProperties.RegistrationSamlProperties bare = new SamlProperties.RegistrationSamlProperties();
        bare.setSpEntityId(null);
        Assert.assertNull(bare.getRegistrationId());
        Assert.assertNull(bare.resolveDisplayName());
    }
}
