package com.kewen.framework.idaas.saml;

import com.kewen.framework.auth.security.model.SecurityUser;
import com.kewen.framework.auth.security.response.SecurityUserConverter;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * 2026/02/14
 *
 * @author kewen
 * @since 1.0
 */
public class SamlSecurityUserConverter implements SecurityUserConverter {
    @Override
    public boolean support(HttpServletRequest request, Object principal) {
        return principal instanceof Saml2AuthenticatedPrincipal;
    }

    @Override
    public SecurityUser convert(HttpServletRequest request, Object principal) {
        Saml2AuthenticatedPrincipal saml2AuthenticatedPrincipal = (Saml2AuthenticatedPrincipal) principal;
        String name = saml2AuthenticatedPrincipal.getName();
        SecurityUser securityUser = new SecurityUser();
        securityUser.setName(name);
        return securityUser;
    }
}
