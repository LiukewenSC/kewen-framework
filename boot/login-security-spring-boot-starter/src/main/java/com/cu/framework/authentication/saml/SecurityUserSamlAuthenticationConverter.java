package com.cu.framework.authentication.saml;

import com.cu.framework.authentication.exception.SsoErrorCodes;
import com.cu.framework.authentication.model.SecurityUser;
import com.cu.framework.authentication.service.SecurityUserDetailsService;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Response;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml2.core.Saml2Error;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml5AuthenticationProvider;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml5AuthenticationProvider.ResponseToken;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticationException;

import java.util.ArrayList;
import java.util.List;

/**
 * 把 SAML2 断言校验后的组装结果换成本框架的统一认证主体
 * <p>
 * 断言的签名、条件、主体等校验都在 {@code OpenSaml5AuthenticationProvider#authenticate} 的 process
 * 阶段完成，本类只接管它之后的组装环节，因此替换转换器不会减少任何校验。属性、NameID 与服务注册标识
 * 沿用 Spring 默认转换器的产出（含断言属性的类型归一化），额外补上会话索引：默认转换器用的构造不带
 * 会话索引，而服务发起的单点登出需要它才能对上身份源侧的会话。
 *
 * @author kewen
 * @since 2026-10-18
 */
public class SecurityUserSamlAuthenticationConverter
        implements Converter<ResponseToken, Saml2Authentication> {

    /**
     * Spring 的默认组装策略，用它取回已完成校验的断言属性、NameID 与服务注册标识
     */
    private final Converter<ResponseToken, Saml2Authentication> defaultConverter =
            OpenSaml5AuthenticationProvider.createDefaultResponseAuthenticationConverter();

    /**
     * 本系统账号来源，断言只负责证明"这个人是谁"，权限一律来自本系统
     */
    private final SecurityUserDetailsService userDetailsService;

    public SecurityUserSamlAuthenticationConverter(SecurityUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Saml2Authentication convert(ResponseToken responseToken) {
        Saml2Authentication nativeAuthentication = defaultConverter.convert(responseToken);
        Saml2AuthenticatedPrincipal nativePrincipal =
                (Saml2AuthenticatedPrincipal) nativeAuthentication.getPrincipal();
        SecurityUser localUser = loadLocalUser(nativePrincipal.getName());
        SamlSecurityUser principal = SamlSecurityUser.of(localUser, nativePrincipal.getAttributes(),
                sessionIndexes(responseToken.getResponse()), nativePrincipal.getRelyingPartyRegistrationId());
        return new Saml2Authentication(principal, nativeAuthentication.getSaml2Response(), principal.getAuthorities());
    }

    /**
     * 以断言的 NameID 作为本系统用户名取账号，取不到时以可区分的错误码终止本次认证，
     * 而不是放一个没有权限的会话进来
     */
    private SecurityUser loadLocalUser(String nameId) {
        try {
            return (SecurityUser) userDetailsService.loadUserByUsername(nameId);
        } catch (UsernameNotFoundException e) {
            Saml2Error error = new Saml2Error(SsoErrorCodes.LOCAL_USER_NOT_FOUND,
                    "断言认证的账号 [" + nameId + "] 在本系统中不存在");
            throw new Saml2AuthenticationException(error, error.toString());
        }
    }

    /**
     * 取断言中 AuthnStatement 的会话索引，与默认转换器一致只看第一个断言
     */
    private List<String> sessionIndexes(Response response) {
        List<String> indexes = new ArrayList<>();
        Assertion assertion = response.getAssertions().get(0);
        for (AuthnStatement statement : assertion.getAuthnStatements()) {
            String sessionIndex = statement.getSessionIndex();
            if (sessionIndex != null) {
                indexes.add(sessionIndex);
            }
        }
        return indexes;
    }
}
