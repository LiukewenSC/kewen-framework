package com.cu.framework.authentication.saml;

import com.cu.framework.authentication.model.SecurityUser;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SAML2 单点登录的统一认证主体：账号信息与权限由本系统提供并复制自本地加载的用户，
 * 断言的原始属性、会话索引与服务注册标识留在本类型上，供服务发起的单点登出使用
 * <p>
 * 之所以必须保留这些原生数据而不是让 {@link SecurityUser} 直接充当主体：
 * Spring Security 的登出解析器以 {@code principal instanceof Saml2AuthenticatedPrincipal}
 * 为前提取服务注册标识，取不到时整个登出请求不会生成。
 * <p>
 * 不能同时实现 {@code OAuth2User}：两个接口的 {@code getAttributes()} 返回类型互不兼容，
 * 因此 OAuth2/OIDC 的主体另见 {@code Oauth2SecurityUser}。
 *
 * @author kewen
 * @since 2026-10-18
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class SamlSecurityUser extends SecurityUser implements Saml2AuthenticatedPrincipal {

    /**
     * 断言中的原始属性，SAML 属性为多值
     */
    private Map<String, List<Object>> attributes = Collections.emptyMap();

    /**
     * 断言 AuthnStatement 中的会话索引，供单点登出定位身份源会话
     */
    private List<String> sessionIndexes = Collections.emptyList();

    /**
     * 本次登录所属的服务注册标识
     */
    private String relyingPartyRegistrationId;

    /**
     * 以本系统加载到的账号为数据源构造认证主体，并挂上断言的原始数据
     *
     * @param localUser     本系统用户详情服务加载的账号，其字段逐项复制到本对象，权限以此为准
     * @param attributes    断言的原始属性，可为 null
     * @param sessionIndexes 断言中的会话索引，可为 null
     * @param relyingPartyRegistrationId 本次登录所属的服务注册标识
     * @return 统一认证主体
     */
    public static SamlSecurityUser of(SecurityUser localUser, Map<String, List<Object>> attributes,
                                      List<String> sessionIndexes, String relyingPartyRegistrationId) {
        SamlSecurityUser principal = new SamlSecurityUser();
        BeanUtils.copyProperties(localUser, principal);
        principal.setAttributes(attributes == null
                ? Collections.<String, List<Object>>emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<>(attributes)));
        principal.setSessionIndexes(sessionIndexes == null
                ? Collections.<String>emptyList()
                : Collections.unmodifiableList(new ArrayList<>(sessionIndexes)));
        principal.setRelyingPartyRegistrationId(relyingPartyRegistrationId);
        return principal;
    }
}
