package com.kewen.framework.idaas.oauth2;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationProvider;
import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * 2026/02/20
 *
 * @author kewen
 * @since 1.0
 */
public class IdaasSPOAuth2LoginAuthenticationProvider implements AuthenticationProvider {

    private OAuth2LoginAuthenticationProvider delegate;

    public IdaasSPOAuth2LoginAuthenticationProvider(OAuth2LoginAuthenticationProvider delegate) {
        this.delegate = delegate;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Authentication authenticate = delegate.authenticate(authentication);
        OAuth2User oAuth2User = (OAuth2User)authenticate.getPrincipal();

        return authenticate;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return delegate.supports(authentication);
    }
}
