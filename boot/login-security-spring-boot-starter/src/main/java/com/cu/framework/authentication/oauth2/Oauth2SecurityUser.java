package com.cu.framework.authentication.oauth2;

import com.cu.framework.authentication.model.SecurityUser;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * OAuth2 / OIDC 单点登录的统一认证主体：账号信息与权限由本系统提供并复制自本地加载的用户，
 * 身份源返回的原始声明留在本类型上，供依赖认证主体的协议级功能使用
 * <p>
 * 之所以实现 {@link OidcUser} 而不单独为 OIDC 建一个类型：{@code OidcUser} 是
 * {@code OAuth2User} 的超集，纯 OAuth2 客户端没有 ID Token，此时 {@link #getIdToken()} 返回 null，
 * 依赖 ID Token 的功能（如 relying party 发起的会话终止）只对 OIDC 客户端有意义。
 * <p>
 * 不能同时实现 {@code Saml2AuthenticatedPrincipal}：两个接口的 {@code getAttributes()} 返回类型
 * 分别为 {@code Map<String, Object>} 与 {@code Map<String, List<Object>>}，同一个类无法兼容，
 * 因此 SAML2 的主体另见 {@code SamlSecurityUser}。
 *
 * @author kewen
 * @since 2026-10-18
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class Oauth2SecurityUser extends SecurityUser implements OidcUser {

    /**
     * 身份源返回的原始声明；OIDC 下为 ID Token 与 UserInfo 合并后的声明集
     */
    private Map<String, Object> attributes = Collections.emptyMap();

    /**
     * OIDC 的 ID Token，纯 OAuth2 客户端为 null
     */
    private OidcIdToken idToken;

    /**
     * OIDC 的 UserInfo 声明，未拉取用户信息端点时为 null
     */
    private OidcUserInfo userInfo;

    /**
     * 以本系统加载到的账号为数据源构造认证主体，并挂上身份源的原始数据
     *
     * @param localUser  本系统用户详情服务加载的账号，其字段逐项复制到本对象，权限以此为准
     * @param attributes 身份源返回的原始声明，可为 null
     * @param idToken    OIDC 的 ID Token，纯 OAuth2 客户端传 null
     * @param userInfo   OIDC 的 UserInfo，未拉取时传 null
     * @return 统一认证主体
     */
    public static Oauth2SecurityUser of(SecurityUser localUser, Map<String, Object> attributes,
                                        OidcIdToken idToken, OidcUserInfo userInfo) {
        Oauth2SecurityUser principal = new Oauth2SecurityUser();
        BeanUtils.copyProperties(localUser, principal);
        principal.setAttributes(attributes == null
                ? Collections.<String, Object>emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<>(attributes)));
        principal.setIdToken(idToken);
        principal.setUserInfo(userInfo);
        return principal;
    }

    /**
     * {@inheritDoc}
     * <p>与 Spring 自身的 {@code DefaultOidcUser} 一致，声明集即 {@link #getAttributes()}；
     * 本对象没有独立存储的声明映射，两处始终相同。
     */
    @Override
    public Map<String, Object> getClaims() {
        return getAttributes();
    }
}
