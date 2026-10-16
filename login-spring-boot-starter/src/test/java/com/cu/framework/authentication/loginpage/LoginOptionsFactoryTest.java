package com.cu.framework.authentication.loginpage;

import com.cu.framework.authentication.oauth2.Oauth2OidcProperties;
import com.cu.framework.authentication.oauth2.Oauth2OidcProperties.Oauth2OidcRegistrationClientProperties;
import com.cu.framework.authentication.oauth2.OauthProtocolType;
import com.cu.framework.authentication.password.properties.SecurityLoginProperties;
import com.cu.framework.authentication.saml.properties.SamlProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 登录方式清单的装配正确性：条数随配置变化、展示名回退、发起地址拼接、缺标识条目被排除、响应不含机密
 *
 * @author kewen
 * @since 2026-10-16
 */
public class LoginOptionsFactoryTest {

    private static final String CLIENT_SECRET = "35fcd41f49ed477992cc9ff6dbbd4e19";
    private static final String KEYSTORE_PASSWORD = "changeit-secret";

    private SecurityLoginProperties passwordProperties() {
        return new SecurityLoginProperties();
    }

    private Oauth2OidcRegistrationClientProperties oauth2Client(String registrationId, OauthProtocolType type, String clientName) {
        Oauth2OidcRegistrationClientProperties client = new Oauth2OidcRegistrationClientProperties();
        client.setRegistrationId(registrationId);
        client.setProtocolType(type);
        client.setClientName(clientName);
        client.setClientId("client_50d901b5844247df");
        client.setClientSecret(CLIENT_SECRET);
        client.setAuthorizationUri("http://localhost:9010/oauth2/authorize");
        client.setTokenUri("http://localhost:9010/oauth2/token");
        client.setUserInfoUri("http://localhost:9010/oauth2/userinfo");
        return client;
    }

    private SamlProperties.RegistrationSamlProperties samlRegistration(String registrationId, String displayName) {
        SamlProperties.RegistrationSamlProperties registration = new SamlProperties.RegistrationSamlProperties();
        registration.setRegistrationId(registrationId);
        registration.setDisplayName(displayName);
        registration.setSpEntityId("cu-framework-local");
        return registration;
    }

    private LoginOptionsFactory factoryOf(List<Oauth2OidcRegistrationClientProperties> clients,
                                          List<SamlProperties.RegistrationSamlProperties> registrations) {
        Oauth2OidcProperties oauth2 = new Oauth2OidcProperties();
        oauth2.setClients(clients);
        SamlProperties saml = new SamlProperties();
        saml.setRegistrations(registrations);
        saml.setSpSigningKeystore(spSigningKeystore());
        return new LoginOptionsFactory(passwordProperties(), oauth2, saml);
    }

    private SamlProperties.SpSigningKeystore spSigningKeystore() {
        SamlProperties.SpSigningKeystore keystore = new SamlProperties.SpSigningKeystore();
        keystore.setKeystorePassword(KEYSTORE_PASSWORD);
        keystore.setKeyPassword(KEYSTORE_PASSWORD);
        return keystore;
    }

    /**
     * 各配一条身份源时返回两条，协议类别分别为 oauth2 与 saml，账号密码段同时给出
     */
    @Test
    public void oneEntryPerConfiguredRegistration() {
        LoginOptions options = factoryOf(
                Collections.singletonList(oauth2Client("demo-oauth2", OauthProtocolType.OAUTH2, "演示 OAuth2")),
                Collections.singletonList(samlRegistration("demo-saml", "企业单点登录"))).create();

        Assert.assertEquals(2, options.getSso().size());
        Assert.assertEquals("oauth2", options.getSso().get(0).getProtocol());
        Assert.assertEquals("saml", options.getSso().get(1).getProtocol());
        Assert.assertEquals("演示 OAuth2", options.getSso().get(0).getDisplayName());
        Assert.assertEquals("企业单点登录", options.getSso().get(1).getDisplayName());

        Assert.assertEquals("/login", options.getPassword().getLoginUrl());
        Assert.assertEquals("username", options.getPassword().getUsernameParameter());
        Assert.assertEquals("password", options.getPassword().getPasswordParameter());
    }

    /**
     * 追加一条配置即多一个入口，前端无需改动
     */
    @Test
    public void entryCountFollowsConfiguration() {
        LoginOptions options = factoryOf(
                Arrays.asList(oauth2Client("first", OauthProtocolType.OAUTH2, null),
                        oauth2Client("second", OauthProtocolType.OIDC, null)),
                Collections.singletonList(samlRegistration("demo-saml", null))).create();

        Assert.assertEquals(3, options.getSso().size());
        Assert.assertEquals("oidc", options.getSso().get(1).getProtocol());
        Assert.assertEquals("first", options.getSso().get(0).getDisplayName());
        Assert.assertEquals("second", options.getSso().get(1).getDisplayName());
        Assert.assertEquals("demo-saml", options.getSso().get(2).getDisplayName());
    }

    /**
     * 手动端点模式的客户端没把展示名写进协议注册对象，清单仍要拿到展示名，且发起地址走授权端点前缀
     */
    @Test
    public void manualEndpointModeStillYieldsDisplayNameAndAuthorizationUrl() {
        LoginOptions options = factoryOf(
                Collections.singletonList(oauth2Client("demo-oauth2", OauthProtocolType.OAUTH2, null)),
                new ArrayList<>()).create();
        LoginOptions.Sso entry = options.getSso().get(0);
        Assert.assertEquals("demo-oauth2", entry.getDisplayName());
        Assert.assertEquals("/oauth2/authorization/demo-oauth2", entry.getLoginUrl());
    }

    /**
     * 两类协议的发起地址用同一个字段名给出，SAML 走固定的认证发起路径
     */
    @Test
    public void bothProtocolsExposeLoginUrlUnderSameField() {
        LoginOptions options = factoryOf(
                Collections.singletonList(oauth2Client("demo-oauth2", OauthProtocolType.OAUTH2, null)),
                Collections.singletonList(samlRegistration("demo-saml", null))).create();
        Assert.assertEquals("/oauth2/authorization/demo-oauth2", options.getSso().get(0).getLoginUrl());
        Assert.assertEquals("/saml2/authenticate/demo-saml", options.getSso().get(1).getLoginUrl());
    }

    /**
     * SAML 未配 registration-id 时按既有语义回退到 sp-entity-id，而不是被丢弃
     */
    @Test
    public void samlFallsBackToSpEntityId() {
        LoginOptions options = factoryOf(new ArrayList<>(),
                Collections.singletonList(samlRegistration(null, null))).create();
        Assert.assertEquals(1, options.getSso().size());
        Assert.assertEquals("cu-framework-local", options.getSso().get(0).getRegistrationId());
        Assert.assertEquals("/saml2/authenticate/cu-framework-local", options.getSso().get(0).getLoginUrl());
    }

    /**
     * 拼不出有效地址的客户端条目被排除，其余条目照常返回
     */
    @Test
    public void clientWithoutRegistrationIdIsExcluded() {
        LoginOptions options = factoryOf(
                Arrays.asList(oauth2Client(null, OauthProtocolType.OAUTH2, null),
                        oauth2Client("ok-client", OauthProtocolType.OAUTH2, null)),
                new ArrayList<>()).create();
        Assert.assertEquals(1, options.getSso().size());
        Assert.assertEquals("ok-client", options.getSso().get(0).getRegistrationId());
    }

    /**
     * 未认证可访问的接口只暴露展示所需字段，密钥、口令与证书配置一概不出现
     */
    @Test
    public void responseCarriesNoSecrets() throws Exception {
        LoginOptions options = factoryOf(
                Collections.singletonList(oauth2Client("demo-oauth2", OauthProtocolType.OAUTH2, "演示 OAuth2")),
                Collections.singletonList(samlRegistration("demo-saml", "企业单点登录"))).create();
        String json = new ObjectMapper().writeValueAsString(options);

        Assert.assertFalse("不得泄露客户端密钥", json.contains(CLIENT_SECRET));
        Assert.assertFalse("不得泄露密钥库口令", json.contains(KEYSTORE_PASSWORD));
        Assert.assertFalse("不得泄露 client-id", json.contains("client_50d901b5844247df"));
        Assert.assertFalse("不得泄露授权与令牌端点", json.contains("9010"));
        Assert.assertFalse("不得出现记住我参数名", json.contains("remember"));
        Assert.assertTrue(json.contains("displayName"));
        Assert.assertTrue(json.contains("loginUrl"));
    }

    /**
     * 完全没有身份源配置时返回空列表而不是失败，账号密码段仍然可用
     */
    @Test
    public void absentConfigYieldsEmptySsoList() {
        LoginOptions options = factoryOf(null, null).create();
        Assert.assertTrue(options.getSso().isEmpty());
        Assert.assertEquals("/login", options.getPassword().getLoginUrl());
    }
}
