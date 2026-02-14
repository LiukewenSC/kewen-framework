package com.kewen.framework.idaas.saml;

import com.kewen.framework.auth.security.model.SecurityUser;
import com.kewen.framework.auth.security.response.SecurityUserParser;
import org.springframework.security.core.Authentication;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;

import javax.servlet.http.HttpServletRequest;

/**
 * 2026/02/14
 *
 * @author kewen
 * @since 1.0
 */
public class SamlSecurityUserParser implements SecurityUserParser {
    @Override
    public boolean support(HttpServletRequest request, Authentication authentication) {
        return authentication instanceof Saml2Authentication;
    }

    @Override
    public SecurityUser convert(HttpServletRequest request, Authentication authentication) {
        Saml2Authentication saml2Authentication = (Saml2Authentication) authentication;
        Saml2AuthenticatedPrincipal saml2AuthenticatedPrincipal = (Saml2AuthenticatedPrincipal) saml2Authentication.getPrincipal();
        String name = saml2AuthenticatedPrincipal.getName();
        SecurityUser securityUser = new SecurityUser();
        securityUser.setName(name);
        return securityUser;
    }
}
