package com.kewen.framework.idaas.saml;

import com.kewen.framework.auth.rabc.composite.SysUserComposite;
import com.kewen.framework.auth.rabc.composite.model.SimpleAuthObject;
import com.kewen.framework.auth.rabc.mp.entity.SysUser;
import com.kewen.framework.auth.rabc.mp.entity.SysUserCredential;
import com.kewen.framework.auth.rabc.model.UserAuthObject;
import com.kewen.framework.auth.security.model.SecurityUser;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml2.provider.service.authentication.OpenSamlAuthenticationProvider;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;

import java.util.Collection;

/**
 * 自定义 SAML 认证提供者
 * 在 SAML 认证成功后，从数据库加载完整的用户信息和权限，替换默认的简单用户信息
 *
 * @author kewen
 * @since 1.0
 */
public class SamlAuthenticationProvider implements AuthenticationProvider {

    private final OpenSamlAuthenticationProvider delegate;
    private final UserDetailsService userDetailsService;

    public SamlAuthenticationProvider(OpenSamlAuthenticationProvider delegate, UserDetailsService userDetailsService) {
        this.delegate = delegate;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // 1. 调用默认的 OpenSamlAuthenticationProvider 进行认证
        Saml2Authentication saml2Authentication = (Saml2Authentication) delegate.authenticate(authentication);

        // 2. 从 SAML 认证中提取用户名
        Saml2AuthenticatedPrincipal principal = (Saml2AuthenticatedPrincipal) saml2Authentication.getPrincipal();
        String username = principal.getName();

        // 3. 从数据库加载完整的用户信息和权限
        SecurityUser securityUser = (SecurityUser) userDetailsService.loadUserByUsername(username);
        if (securityUser == null) {
            throw new UsernameNotFoundException("未找到用户");
        }
        return new Saml2Authentication(
                securityUser,
                saml2Authentication.getSaml2Response(),
                securityUser.getAuthorities()
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return delegate.supports(authentication);
    }
}
