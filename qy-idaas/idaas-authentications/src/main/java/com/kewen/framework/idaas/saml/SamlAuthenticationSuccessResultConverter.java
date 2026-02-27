package com.kewen.framework.idaas.saml;

import com.kewen.framework.auth.security.model.SecurityUser;
import com.kewen.framework.auth.security.response.AuthenticationSuccessResultConverter;
import com.kewen.framework.auth.security.response.JsonSuccessResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;

/**
 * 2026/02/27
 *
 * @author kewen
 * @since 4.24.0-SP01
 */
public class SamlAuthenticationSuccessResultConverter implements AuthenticationSuccessResultConverter {
    @Override
    public boolean support(Authentication authentication) {
        return authentication.getPrincipal() instanceof Saml2AuthenticatedPrincipal;
    }

    @Override
    public JsonSuccessResult convert(Authentication authentication) {
        Saml2AuthenticatedPrincipal principal = (Saml2AuthenticatedPrincipal) authentication.getPrincipal();
        JsonSuccessResult result = new JsonSuccessResult();
        result.setName(principal.getName());
        result.setNickName(principal.getRelyingPartyRegistrationId());
        return result;
    }

    @Override
    public SecurityUser convert(UserDetailsService userDetailsService, Authentication authentication) {
        Saml2AuthenticatedPrincipal principal = (Saml2AuthenticatedPrincipal) authentication.getPrincipal();
        String name = principal.getName();
        return (SecurityUser) userDetailsService.loadUserByUsername(name);
    }
}
