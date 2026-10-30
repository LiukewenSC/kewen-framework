package com.cu.framework.authentication.saml;

import com.cu.framework.authentication.model.SecurityUser;
import com.cu.framework.authentication.model.SecurityUserAuthority;
import com.cu.framework.authentication.service.SecurityUserDetailsService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml5AuthenticationProvider;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml5AuthenticationProvider.ResponseToken;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticationException;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticationToken;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 换上统一认证主体的组装策略之后，断言校验仍在本体之前发生
 *
 * @author kewen
 * @since 2026-10-18
 */
public class SecurityUserSamlAuthenticationConverterTest {

    /**
     * 记录组装环节被调用了几次的转换器包装
     */
    private static class CountingConverter extends SecurityUserSamlAuthenticationConverter {
        final AtomicInteger invocations = new AtomicInteger();

        CountingConverter(SecurityUserDetailsService userDetailsService) {
            super(userDetailsService);
        }

        @Override
        public Saml2Authentication convert(ResponseToken responseToken) {
            invocations.incrementAndGet();
            return super.convert(responseToken);
        }
    }

    private SecurityUserDetailsService unusedUserDetailsService() {
        return new SecurityUserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) {
                return new SecurityUser().setUsername(username).setEnabled(true)
                        .setUserAuthorities(Collections.singletonList(new SecurityUserAuthority("ROLE_USER", null)));
            }

            @Override
            public UserDetails updatePassword(UserDetails user, String encryptedNewPassword) {
                return user;
            }
        };
    }

    private RelyingPartyRegistration registration() {
        return RelyingPartyRegistration.withRegistrationId("demo-saml")
                .entityId("urn:demo:sp")
                .assertionConsumerServiceLocation("http://localhost/login/saml2/sso/demo-saml")
                .assertingPartyMetadata(details -> details
                        .entityId("urn:demo:idp")
                        .singleSignOnServiceLocation("http://localhost/sso")
                        .wantAuthnRequestsSigned(false))
                .build();
    }

    /**
     * 无法解析的 SAML 响应在认证阶段就被拒绝，组装环节一次都没进入
     */
    @Test
    public void invalidResponseIsRejectedBeforeAssembly() {
        OpenSaml5AuthenticationProvider provider = new OpenSaml5AuthenticationProvider();
        CountingConverter converter = new CountingConverter(unusedUserDetailsService());
        provider.setResponseAuthenticationConverter(converter);

        Saml2AuthenticationToken token = new Saml2AuthenticationToken(registration(), "<not-a-saml-response/>", null);

        try {
            provider.authenticate(token);
            Assert.fail("无法解析的断言必须被拒绝");
        } catch (Saml2AuthenticationException e) {
            Assert.assertEquals(0, converter.invocations.get());
        }
    }
}
