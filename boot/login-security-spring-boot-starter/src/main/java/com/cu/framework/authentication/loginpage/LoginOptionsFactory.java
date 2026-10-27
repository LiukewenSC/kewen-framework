package com.cu.framework.authentication.loginpage;

import com.cu.framework.authentication.oauth2.Oauth2OidcProperties;
import com.cu.framework.authentication.oauth2.Oauth2OidcProperties.Oauth2OidcRegistrationClientProperties;
import com.cu.framework.authentication.password.properties.SecurityLoginProperties;
import com.cu.framework.authentication.saml.properties.SamlProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 从框架自身的配置属性装配 {@link LoginOptions}。
 * <p>
 * 数据来源是 {@code Oauth2OidcProperties} 与 {@code SamlProperties} 这两个属性类，而不是 Spring Security 的
 * {@code ClientRegistrationRepository} 与 {@code RelyingPartyRegistrationRepository}：前者才是配置的真相源，
 * 后两者在本 starter 里一个不是 Bean（{@code Oauth2Config} 内私有构造，同一份配置会被重复构建多次），
 * 另一个的 {@code ClientRegistration} 在手动端点模式下根本不写入展示名。
 *
 * @author kewen
 * @since 2026-10-16
 */
public class LoginOptionsFactory {

    private static final Logger log = LoggerFactory.getLogger(LoginOptionsFactory.class);

    /**
     * SAML 认证发起路径前缀。Spring Security 5.7.11 的 Saml2LoginConfigurer 中该字段为私有且无 setter，
     * 配了也不会生效，因此这里取常量而不做成配置项
     */
    private static final String SAML_AUTHENTICATION_URI = "/saml2/authenticate";

    private static final String PROTOCOL_SAML = "saml";

    private final SecurityLoginProperties passwordProperties;
    private final Oauth2OidcProperties oauth2Properties;
    private final SamlProperties samlProperties;

    public LoginOptionsFactory(SecurityLoginProperties passwordProperties,
                               Oauth2OidcProperties oauth2Properties,
                               SamlProperties samlProperties) {
        this.passwordProperties = passwordProperties;
        this.oauth2Properties = oauth2Properties;
        this.samlProperties = samlProperties;
    }

    /**
     * 装配当前可用的登录方式清单
     *
     * @return 账号密码信息加单点登录入口列表，入口条数等于配置有效（能解析出注册标识）的身份源条数
     */
    public LoginOptions create() {
        LoginOptions options = new LoginOptions();

        LoginOptions.Password password = options.getPassword();
        password.setLoginUrl(passwordProperties.getLoginUrl());
        password.setUsernameParameter(passwordProperties.getUsernameParameter());
        password.setPasswordParameter(passwordProperties.getPasswordParameter());

        List<LoginOptions.Sso> entries = new ArrayList<>();
        entries.addAll(buildOauth2Entries());
        entries.addAll(buildSamlEntries());
        options.setSso(entries);
        return options;
    }

    /**
     * 按 clients 列表生成 OAuth2 与 OIDC 入口，协议类别直接取自 protocol-type 配置
     */
    private List<LoginOptions.Sso> buildOauth2Entries() {
        List<LoginOptions.Sso> entries = new ArrayList<>();
        List<Oauth2OidcRegistrationClientProperties> clients = oauth2Properties.getClients();
        if (clients == null) {
            return entries;
        }
        String prefix = oauth2Properties.getSsoAuthorizationUri();
        for (int index = 0; index < clients.size(); index++) {
            Oauth2OidcRegistrationClientProperties client = clients.get(index);
            String location = "cu.security.login.oauth2.clients[" + index + "]";
            String registrationId = client.getRegistrationId();
            if (!StringUtils.hasText(registrationId)) {
                log.warn("{} 未配置 registration-id，无法构成有效发起地址，已从登录方式清单中排除", location);
                continue;
            }
            LoginOptions.Sso entry = new LoginOptions.Sso();
            entry.setProtocol(client.getProtocolType() == null
                    ? "oauth2" : client.getProtocolType().name().toLowerCase(Locale.ROOT));
            entry.setRegistrationId(registrationId);
            entry.setDisplayName(StringUtils.hasText(client.getClientName())
                    ? client.getClientName() : registrationId);
            entry.setLoginUrl(joinPath(prefix, registrationId));
            entries.add(entry);
        }
        return entries;
    }

    /**
     * 按 registrations 列表生成 SAML 入口；展示名由属性自身完成向注册标识、再向 SP 实体标识的回退
     */
    private List<LoginOptions.Sso> buildSamlEntries() {
        List<LoginOptions.Sso> entries = new ArrayList<>();
        List<SamlProperties.RegistrationSamlProperties> registrations = samlProperties.getRegistrations();
        if (registrations == null) {
            return entries;
        }
        for (int index = 0; index < registrations.size(); index++) {
            SamlProperties.RegistrationSamlProperties registration = registrations.get(index);
            String location = "cu.security.login.saml.registrations[" + index + "]";
            String registrationId = registration.getRegistrationId();
            if (!StringUtils.hasText(registrationId)) {
                log.warn("{} 的 registration-id 与 sp-entity-id 均为空，无法构成有效发起地址，已从登录方式清单中排除", location);
                continue;
            }
            LoginOptions.Sso entry = new LoginOptions.Sso();
            entry.setProtocol(PROTOCOL_SAML);
            entry.setRegistrationId(registrationId);
            entry.setDisplayName(registration.resolveDisplayName());
            entry.setLoginUrl(joinPath(SAML_AUTHENTICATION_URI, registrationId));
            entries.add(entry);
        }
        return entries;
    }

    /**
     * 拼接路径前缀与注册标识，容忍前缀自带或少带尾斜杠
     */
    private static String joinPath(String prefix, String registrationId) {
        if (!StringUtils.hasText(prefix)) {
            return "/" + registrationId;
        }
        return prefix.endsWith("/") ? prefix + registrationId : prefix + "/" + registrationId;
    }
}
