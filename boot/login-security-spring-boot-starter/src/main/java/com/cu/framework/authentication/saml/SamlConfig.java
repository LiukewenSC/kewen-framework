package com.cu.framework.authentication.saml;

import com.cu.framework.authentication.config.HttpSecurityCustomizer;
import com.cu.framework.authentication.config.SamlConfiguredCondition;
import com.cu.framework.authentication.response.SecurityAuthenticationExceptionResolverHandler;
import com.cu.framework.authentication.response.SecurityAuthenticationSuccessHandler;
import com.cu.framework.authentication.saml.properties.SamlProperties;
import com.cu.framework.authentication.service.SecurityUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.saml2.core.Saml2X509Credential;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml5AuthenticationProvider;
import org.springframework.security.saml2.provider.service.metadata.OpenSaml5MetadataResolver;
import org.springframework.security.saml2.provider.service.registration.InMemoryRelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.web.authentication.Saml2WebSsoAuthenticationFilter;
import org.springframework.security.saml2.provider.service.web.DefaultRelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.RelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.Saml2MetadataFilter;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SAML2 认证配置
 * <p>
 * 通过实现 HttpSecurityCustomizer 接口，将 SAML2 登录配置注入到 Spring Security 过滤器链中。
 * 支持两种配置方式：
 * <ul>
 *   <li>方式一（推荐）：将 IdP 的 metadata.xml 放到 classpath:saml/idp-metadata.xml，自动解析</li>
 *   <li>方式二：手动配置 entityId、webSsoUrl 和证书路径（设置 useMetadata=false）</li>
 * </ul>
 *
 * @author kewen
 * @since 1.0
 */
@AutoConfiguration
@Conditional(SamlConfiguredCondition.class)
@EnableConfigurationProperties({SamlProperties.class})
public class SamlConfig implements HttpSecurityCustomizer{

    private static final Logger log = LoggerFactory.getLogger(SamlConfig.class);

    /** SAML 配置属性（前缀 cu.security.login.saml） */
    @Autowired
    private SamlProperties samlProperties;

    /** SAML 登录成功处理器，配置到 saml2Login 的 successHandler */
    @Autowired
    SecurityAuthenticationSuccessHandler successHandler;
    /** 认证异常处理器，配置到 saml2Login 的 failureHandler */
    @Autowired
    SecurityAuthenticationExceptionResolverHandler exceptionResolverHandler;

    /** 本系统账号来源，用于把断言里的 NameID 换成本系统的用户与权限 */
    @Autowired
    SecurityUserDetailsService securityUserDetailsService;

    /**
     * 添加 SAML2 登录配置到 HttpSecurity
     *
     * @param http HttpSecurity 配置对象
     * @throws Exception 配置异常
     */
    @Override
    public void customizer(HttpSecurity http) throws Exception {
        // Spring Security 7 只保留 OpenSAML 5 支持
        OpenSaml5AuthenticationProvider samlAuthenticationProvider = new OpenSaml5AuthenticationProvider();
        // 断言校验照旧，只接管校验之后的组装环节：让进入安全上下文的主体是本框架的统一认证主体
        samlAuthenticationProvider.setResponseAuthenticationConverter(
                new SecurityUserSamlAuthenticationConverter(securityUserDetailsService));
        //两种方式，
        // 1. 将 SAML2 认证提供者添加到 HttpSecurity，则不需要添加2对应的authenticationManager，保持使用统一的
        // 2. .authenticationManager(new ProviderManager(samlAuthenticationProvider))，那么不需要http.authenticationProvider(samlAuthenticationProvider);
        // 换掉组装主体之后只能选 2：Saml2LoginConfigurer 自己也会向 HttpSecurity 注册一个默认的
        // OpenSaml5AuthenticationProvider，两者共用 AuthenticationManager 时不保证我们这一个先被选中
        // （实测默认那个胜出，进入安全上下文的仍是 DefaultSaml2AuthenticatedPrincipal）。
        http
                .saml2Login(saml2Login -> saml2Login
                        .authenticationManager(new ProviderManager(samlAuthenticationProvider))
                        .relyingPartyRegistrationRepository(relyingPartyRegistrationRepository())
                        .successHandler(successHandler)
                        .failureHandler(exceptionResolverHandler)
                        .loginProcessingUrl(samlProperties.getLoginProcessingUrl()))
                .saml2Logout(saml2Logout -> saml2Logout
                        .logoutUrl("/logout")
                        .logoutRequest(logoutRequest -> logoutRequest.logoutUrl("/logout/saml2/slo"))
                        .logoutResponse(logoutResponse -> logoutResponse.logoutUrl("/logout/saml2/slo")));
        // 添加SAML sp Metadata过滤器，spring-security 默认是不添加sp的metadata的
        // 默认的路径为 /saml2/service-provider-metadata/{registrationId}
        Saml2MetadataFilter saml2MetadataFilter = new Saml2MetadataFilter(
                (RelyingPartyRegistrationResolver)new DefaultRelyingPartyRegistrationResolver(relyingPartyRegistrationRepository()),
                new OpenSaml5MetadataResolver()
        );
        http.addFilterBefore(saml2MetadataFilter, Saml2WebSsoAuthenticationFilter.class);
    }

    /**
     * 配置 SAML2 Relying Party 注册信息。
     * 根据 useMetadata 配置决定从 metadata.xml 自动解析还是使用手动配置
     *
     * @return RelyingPartyRegistrationRepository
     */
    @Bean
    public RelyingPartyRegistrationRepository relyingPartyRegistrationRepository() {
        List<RelyingPartyRegistration> registration;
        //if (samlProperties.isUseMetadata()) {
            registration = buildRegistrationFromMetadata(samlProperties);
        //} else {
            //registration = buildRegistrationFromProperties();
        //}
        return new InMemoryRelyingPartyRegistrationRepository(registration);
    }

    /**
     * 从 IdP metadata.xml 中自动解析并构建 RelyingPartyRegistration
     */
    private List<RelyingPartyRegistration> buildRegistrationFromMetadata(SamlProperties samlProperties) {
        return samlProperties.getRegistrations().stream().map(
                samlProperty -> buildRegistrationFromMetadata(samlProperties,samlProperty, samlProperties.getLoginProcessingUrl())
        ).collect(Collectors.toList());
    }
    /**
     * 解析单个注册项的 IdP metadata.xml 并构建 RelyingPartyRegistration：
     * 设置 SP 的 registrationId、entityId、ACS 地址与登出地址，
     * 从元数据填充 IdP 的 entityId、SSO 地址、绑定方式与验签证书；
     * 元数据中存在 SingleLogoutService 时补充登出绑定，配置了 keystore 时附加 SP 签名凭证
     * @param samlProperties SAML 全局配置
     * @param spSamlProperties 当前注册项配置
     * @param loginProcessingUrl SP 的 ACS（登录回调）路径
     * @return 构建完成的 RelyingPartyRegistration
     */
    private RelyingPartyRegistration buildRegistrationFromMetadata(SamlProperties samlProperties, SamlProperties.RegistrationSamlProperties spSamlProperties,String loginProcessingUrl) {
        IdpMetadataParser parser = new IdpMetadataParser();
        IdpMetadataParser.IdpMetadata metadata = parser.parse(spSamlProperties.getMetadataResource());

        log.info("从 metadata.xml 解析成功: entityId={}, ssoUrl={}", metadata.getEntityId(), metadata.getSsoUrl());

        Saml2X509Credential verificationCredential = Saml2X509Credential.verification(metadata.getSigningCertificate());

        RelyingPartyRegistration.Builder builder = RelyingPartyRegistration
                .withRegistrationId(spSamlProperties.getRegistrationId())
                .entityId(spSamlProperties.getSpEntityId())
                .assertionConsumerServiceLocation("{baseUrl}" + loginProcessingUrl)
                // 如果使用SP退出联动IDP退出，则必须设置此处，否则
                // org/springframework/security/saml2/provider/service/web/authentication/logout/Saml2LogoutResponseFilter.java:123 处
                //registration.getSingleLogoutServiceResponseLocation() 为空，会报错
                //同时org.springframework.security.saml2.provider.service.authentication.logout.OpenSamlLogoutResponseValidator.validateDestination
                // 也会使用全匹配校验destination是否匹配
                .singleLogoutServiceLocation("{baseUrl}"+samlProperties.getLogoutRequestUrl())
                .singleLogoutServiceResponseLocation("{baseUrl}"+samlProperties.getLogoutResponseUrl())
                .assertingPartyMetadata(assertingParty -> assertingParty
                        .entityId(metadata.getEntityId())
                        .wantAuthnRequestsSigned(metadata.isWantAuthnRequestsSigned())
                        .singleSignOnServiceLocation(metadata.getSsoUrl())
                        .singleSignOnServiceBinding(metadata.getSsoBinding())
                        .verificationX509Credentials(credentials -> credentials.add(verificationCredential))
                )
        ;

        // 只在 metadata 中有 SingleLogoutService 时才设置退出相关属性
        if (metadata.getLogoutUrl() != null && metadata.getLogoutBinding() != null) {
            builder.singleLogoutServiceBinding(metadata.getLogoutBinding())
                    .assertingPartyMetadata(assertingParty -> assertingParty
                            .singleLogoutServiceLocation(metadata.getLogoutUrl())
                            .singleLogoutServiceBinding(metadata.getLogoutBinding())
                    );
        }

        // 配置 SP 签名凭证（用于签名 AuthnRequest 和 LogoutRequest）
        Saml2X509Credential signingCredential = loadSpSigningCredential();
        if (signingCredential != null) {
            builder.signingX509Credentials(credentials -> credentials.add(signingCredential));
        }

        return builder.build();
    }

    /**
     * 从 keystore 加载 SP 签名凭证（私钥+证书），用于签名 SAML AuthnRequest 和 LogoutRequest
     *
     * @return SP 签名凭证，如果未配置 keystore 则返回 null
     */
    private Saml2X509Credential loadSpSigningCredential() {
        SamlProperties.SpSigningKeystore keystoreConfig = samlProperties.getSpSigningKeystore();
        if (keystoreConfig == null || keystoreConfig.getKeystoreResource() == null) {
            return null;
        }

        if (!keystoreConfig.getKeystoreResource().exists()){
            log.warn("SP 签名密钥库不存在: {}", keystoreConfig.getKeystoreResource().getFilename());
            return null;
        }
        try (InputStream inputStream = keystoreConfig.getKeystoreResource().getInputStream()) {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(inputStream, keystoreConfig.getKeystorePassword().toCharArray());

            PrivateKey privateKey = (PrivateKey) keyStore.getKey(
                    keystoreConfig.getKeyAlias(),
                    keystoreConfig.getKeyPassword().toCharArray()
            );
            X509Certificate certificate = (X509Certificate) keyStore.getCertificate(keystoreConfig.getKeyAlias());

            return Saml2X509Credential.signing(privateKey, certificate);
        } catch (Exception e) {
            throw new IllegalStateException("加载 SP 签名密钥库失败: " + keystoreConfig.getKeystoreResource(), e);
        }
    }


    /**
     * SAML2 GET 方式退出控制器 Bean，提供 GET /saml2/logout 自动提交 POST 表单触发退出
     * @return SAML 退出控制器
     */
    @Bean
    public SamlLogoutController samlLogoutController() {
        return new SamlLogoutController();
    }

}
