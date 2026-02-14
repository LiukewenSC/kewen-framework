package com.kewen.framework.idaas.saml;

import com.kewen.framework.auth.security.config.HttpSecurityCustomizer;
import com.kewen.framework.auth.security.response.SecurityAuthenticationExceptionResolverHandler;
import com.kewen.framework.auth.security.response.SecurityAuthenticationSuccessHandler;
import com.kewen.framework.idaas.saml.properties.SamlProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.saml2.credentials.Saml2X509Credential;
import org.springframework.security.saml2.provider.service.authentication.OpenSamlAuthenticationProvider;
import org.springframework.security.saml2.provider.service.registration.InMemoryRelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.Saml2MessageBinding;
import org.springframework.security.saml2.provider.service.servlet.filter.Saml2WebSsoAuthenticationFilter;

import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

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
@Configuration
@EnableConfigurationProperties({SamlProperties.class})
public class SamlConfig implements HttpSecurityCustomizer {

    private static final Logger log = LoggerFactory.getLogger(SamlConfig.class);

    @Autowired
    private SamlProperties samlProperties;

    @Autowired
    SecurityAuthenticationSuccessHandler successHandler;
    @Autowired
    SecurityAuthenticationExceptionResolverHandler exceptionResolverHandler;


    @Bean
    SamlSecurityUserConverter samlSecurityUserConverter(){
        return new SamlSecurityUserConverter();
    }

    /**
     * 添加 SAML2 登录配置到 HttpSecurity
     *
     * @param http HttpSecurity 配置对象
     * @throws Exception 配置异常
     */
    @Override
    public void customizer(HttpSecurity http) throws Exception {
        http.saml2Login(saml2 -> saml2
                .authenticationManager(new ProviderManager(
                        new OpenSamlAuthenticationProvider()
                ))
                .relyingPartyRegistrationRepository(relyingPartyRegistrationRepository())
                .successHandler(successHandler)
                .failureHandler(exceptionResolverHandler)
        );
    }

    /**
     * 配置 SAML2 Relying Party 注册信息。
     * 根据 useMetadata 配置决定从 metadata.xml 自动解析还是使用手动配置
     *
     * @return RelyingPartyRegistrationRepository
     */
    @Bean
    public RelyingPartyRegistrationRepository relyingPartyRegistrationRepository() {
        RelyingPartyRegistration registration;
        if (samlProperties.isUseMetadata()) {
            registration = buildRegistrationFromMetadata();
        } else {
            registration = buildRegistrationFromProperties();
        }
        return new InMemoryRelyingPartyRegistrationRepository(registration);
    }

    /**
     * 从 IdP metadata.xml 中自动解析并构建 RelyingPartyRegistration
     */
    private RelyingPartyRegistration buildRegistrationFromMetadata() {
        IdpMetadataParser parser = new IdpMetadataParser();
        IdpMetadataParser.IdpMetadata metadata = parser.parse(samlProperties.getMetadataResource());

        log.info("从 metadata.xml 解析成功: entityId={}, ssoUrl={}", metadata.getEntityId(), metadata.getSsoUrl());

        Saml2X509Credential verificationCredential = new Saml2X509Credential(
                metadata.getSigningCertificate(),
                Saml2X509Credential.Saml2X509CredentialType.VERIFICATION
        );

        return RelyingPartyRegistration
                .withRegistrationId(samlProperties.getRegistrationId())
                .localEntityIdTemplate(samlProperties.getSpEntityId()) //默认值 {baseUrl}/saml2/service-provider-metadata/{registrationId}
                .assertionConsumerServiceUrlTemplate("{baseUrl}"+ Saml2WebSsoAuthenticationFilter.DEFAULT_FILTER_PROCESSES_URI)
                .providerDetails(providerDetails -> providerDetails
                        .entityId(metadata.getEntityId())
                        .webSsoUrl(metadata.getSsoUrl())
                        .binding(Saml2MessageBinding.POST)
                )
                .credentials(credentials -> credentials.add(verificationCredential))
                .build();
    }

    /**
     * 使用手动配置的属性构建 RelyingPartyRegistration
     */
    private RelyingPartyRegistration buildRegistrationFromProperties() {
        X509Certificate idpCertificate = loadCertificateFromResource(samlProperties.getIdpCertificateResource());

        Saml2X509Credential verificationCredential = new Saml2X509Credential(
                idpCertificate,
                Saml2X509Credential.Saml2X509CredentialType.VERIFICATION
        );

        return RelyingPartyRegistration
                .withRegistrationId(samlProperties.getRegistrationId())
                .localEntityIdTemplate(samlProperties.getSpEntityId())
                .assertionConsumerServiceUrlTemplate("{baseUrl}"+ Saml2WebSsoAuthenticationFilter.DEFAULT_FILTER_PROCESSES_URI)
                .providerDetails(providerDetails -> providerDetails
                        .entityId(samlProperties.getEntityId())
                        .binding(Saml2MessageBinding.POST)
                        .webSsoUrl(samlProperties.getWebSsoUrl())
                )
                .credentials(credentials -> credentials.add(verificationCredential))
                .build();
    }

    /**
     * 从资源文件加载 X509 证书
     */
    private X509Certificate loadCertificateFromResource(Resource certificateResource) {
        try (InputStream inputStream = certificateResource.getInputStream()) {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            return (X509Certificate) certificateFactory.generateCertificate(inputStream);
        } catch (Exception e) {
            throw new IllegalStateException("无法加载 IdP 证书: " + certificateResource, e);
        }
    }
}
