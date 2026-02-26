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
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.saml2.core.Saml2X509Credential;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider;
import org.springframework.security.saml2.provider.service.authentication.OpenSamlAuthenticationProvider;
import org.springframework.security.saml2.provider.service.registration.InMemoryRelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;

import java.io.InputStream;
import java.security.cert.CertificateFactory;
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

    @Autowired
    UserDetailsService userDetailsService;

    /**
     * 添加 SAML2 登录配置到 HttpSecurity
     *
     * @param http HttpSecurity 配置对象
     * @throws Exception 配置异常
     */
    @Override
    public void customizer(HttpSecurity http) throws Exception {
        SamlAuthenticationProvider samlAuthenticationProvider = new SamlAuthenticationProvider(
                new OpenSamlAuthenticationProvider(),
                userDetailsService
        );
        //两种方式，
        // 1. 将 SAML2 认证提供者添加到 HttpSecurity，则不需要添加2对应的authenticationManager，保持使用统一的
        // 2. .authenticationManager(new ProviderManager(samlAuthenticationProvider))，那么不需要http.authenticationProvider(samlAuthenticationProvider);
        http.authenticationProvider(samlAuthenticationProvider);
        http
                .saml2Login()
                //.authenticationManager(new ProviderManager(samlAuthenticationProvider))
                .relyingPartyRegistrationRepository(relyingPartyRegistrationRepository())
                .successHandler(successHandler)
                .failureHandler(exceptionResolverHandler)
                .loginProcessingUrl(samlProperties.getLoginProcessingUrl())
                .and()
                /*.saml2Logout(saml2Logout ->
                        saml2Logout.logoutUrl("/logout/saml2/sso/{registrationId}")
                )*/
        ;
        
        
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
                samlProperty -> buildRegistrationFromMetadata(samlProperty, samlProperties.getLoginProcessingUrl())
        ).collect(Collectors.toList());
    }
    private RelyingPartyRegistration buildRegistrationFromMetadata(SamlProperties.RegistrationSamlProperties samlProperties,String loginProcessingUrl) {
        IdpMetadataParser parser = new IdpMetadataParser();
        IdpMetadataParser.IdpMetadata metadata = parser.parse(samlProperties.getMetadataResource());

        log.info("从 metadata.xml 解析成功: entityId={}, ssoUrl={}", metadata.getEntityId(), metadata.getSsoUrl());

        Saml2X509Credential verificationCredential = Saml2X509Credential.verification(metadata.getSigningCertificate());

        return RelyingPartyRegistration
                .withRegistrationId(samlProperties.getRegistrationId())
                .entityId(samlProperties.getSpEntityId())
                .assertionConsumerServiceLocation("{baseUrl}" + loginProcessingUrl)
                .assertingPartyDetails(assertingParty -> assertingParty
                        .entityId(metadata.getEntityId())
                        .wantAuthnRequestsSigned(metadata.isWantAuthnRequestsSigned())
                        .singleSignOnServiceLocation(metadata.getSsoUrl())
                        .singleSignOnServiceBinding(metadata.getSsoBinding())
                        .singleLogoutServiceLocation(metadata.getLogoutUrl())
                        .singleLogoutServiceBinding(metadata.getLogoutBinding())
                        .verificationX509Credentials(credentials -> credentials.add(verificationCredential))
                )
                .build();
    }
}
