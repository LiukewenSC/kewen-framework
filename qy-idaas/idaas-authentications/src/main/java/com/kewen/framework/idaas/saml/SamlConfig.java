package com.kewen.framework.idaas.saml;

import com.kewen.framework.auth.security.config.HttpSecurityCustomizer;
import org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xmlsec.keyinfo.impl.StaticKeyInfoCredentialResolver;
import org.springframework.boot.autoconfigure.security.saml2.Saml2LoginConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.saml2.credentials.Saml2X509Credential;
import org.springframework.security.saml2.provider.service.authentication.OpenSamlAuthenticationProvider;
import org.springframework.security.saml2.provider.service.registration.InMemoryRelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * 2026/02/13
 *
 * @author kewen
 * @since 1.0
 */
@Configuration
public class SamlConfig implements HttpSecurityCustomizer {
    /**
     * @param http 添加SAML 登录
     *
     * @throws Exception
     * @see Saml2LoginConfiguration.Saml2LoginConfigurerAdapter#configure(HttpSecurity)
     */
    @Override
    public void customizer(HttpSecurity http) throws Exception {
        //http.authorizeRequests((requests) -> requests.anyRequest().authenticated()).saml2Login();
        http.saml2Login(saml2 -> saml2
                .authenticationManager(new ProviderManager(
                        new OpenSamlAuthenticationProvider()
                ))
                .relyingPartyRegistrationRepository(
                        relyingPartyRegistrationRepository()
                )
        );
    }
    @Bean
    public RelyingPartyRegistrationRepository relyingPartyRegistrationRepository() {
        try {
            X509Certificate x509Certificate = (X509Certificate) new CertificateFactory().engineGenerateCertificate(null);
            RelyingPartyRegistration registration = RelyingPartyRegistration.withRegistrationId("my-okta")
                    //.assertionConsumerServiceLocation("{baseUrl}/login/saml2/sso/{registrationId}")
                    .assertionConsumerServiceUrlTemplate("{baseUrl}/login/saml2/sso/{registrationId}")
                    .providerDetails(pd -> pd
                            .entityId("your-app-entity-id")
                            .webSsoUrl("https://dev-xxxxxx.okta.com/app/dev-xxxxxx/sso/saml")
                    )
                    .credentials(c -> c
                            .add(new Saml2X509Credential(x509Certificate, Saml2X509Credential.Saml2X509CredentialType.DECRYPTION))
                    )
                    .build();

            return new InMemoryRelyingPartyRegistrationRepository(registration);
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        }
    }

    // 简化密钥处理（生产环境需加载真实证书）
    @Bean
    public KeyInfoCredentialResolver simpleKeyInfoCredentialResolver() {
        return new StaticKeyInfoCredentialResolver((Credential)null);
    }

}
