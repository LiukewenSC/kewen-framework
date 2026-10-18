package com.cu.framework.authentication.oauth2;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 * OIDC 用户信息服务：ID Token 校验、nonce 校验、用户信息拉取与 sub 一致性校验全部沿用
 * {@link OidcUserService} 的实现，只在拿到协议原生主体之后把它换成本框架的统一认证主体
 *
 * @author kewen
 * @since 2026-10-18
 */
public class SecurityUserOidcUserService extends OidcUserService {

    /**
     * 身份源主体到统一认证主体的装配器
     */
    private final SecurityUserSsoAssembler assembler;

    public SecurityUserOidcUserService(SecurityUserSsoAssembler assembler) {
        this.assembler = assembler;
    }

    /**
     * 沿用父类的 OIDC 校验与声明合并，再把结果装配成统一认证主体
     *
     * @param userRequest OIDC 用户信息请求
     * @return 统一认证主体，仍实现 {@link OidcUser}，因此依赖 ID Token 的会话终止等功能不受影响
     * @throws OAuth2AuthenticationException 协议校验失败，或本系统没有对应账号
     */
    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        return assembler.assemble(super.loadUser(userRequest));
    }
}
