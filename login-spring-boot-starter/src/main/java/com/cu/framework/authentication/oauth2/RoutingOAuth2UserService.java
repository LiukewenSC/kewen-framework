package com.cu.framework.authentication.oauth2;

import com.cu.framework.authentication.oauth2.Oauth2OidcProperties.Oauth2OidcRegistrationClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.HashMap;
import java.util.Map;

/**
 * 按 registrationId 分发的 userinfo 用户信息服务。
 * <p>本类只负责"向哪个 HTTP 客户端发这次 userinfo 请求"，不负责把身份源主体换成本框架的统一认证主体，
 * 后者由 {@link SecurityUserOAuth2UserService} 与 {@link SecurityUserOidcUserService} 在各自协议链路的末端各做一次。
 * <p>是否跳过 SSL 证书校验由每个客户端自己的 {@code ignore-ssl} 决定，默认执行标准校验。
 * <p>之所以按 registrationId 查表而不是用 {@code DelegatingOAuth2UserService}：本仓库依赖的
 * Spring Security 5.7.11 中 {@code OAuth2UserService} 没有 supports() 方法，而
 * {@code DelegatingOAuth2UserService.loadUser} 会对列表里每个 delegate 都调用一次 loadUser，
 * 等于向每个 IDP 各发一次 userinfo 请求。
 * <p>{@code RestOperationBuilder} 的作用不止 SSL：它还替换了消息转换器与错误处理器，
 * 用于容忍部分 IDP 返回的非标准 userinfo 响应（如 HTML 错误页）。
 * 其来历见 docs/说明文档/已废弃实现与决策记录.md 第二节。
 *
 * @author kewen
 * @since 2026-10-19
 */
public class RoutingOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private static final Logger log = LoggerFactory.getLogger(RoutingOAuth2UserService.class);

    /**
     * 执行标准证书校验的用户信息服务。
     * 既作为 ignore-ssl 未开启的客户端的默认实现，也作为 registrationId 查不到配置时的兜底，
     * 使配置缺失时的失败方向是"登录不上"而不是"悄悄不校验证书"
     */
    static final OAuth2UserService<OAuth2UserRequest, OAuth2User> SECURE_FALLBACK =
            new DefaultOAuth2UserService();

    /** registrationId 到用户信息服务的映射，构造期一次定型 */
    private final Map<String, OAuth2UserService<OAuth2UserRequest, OAuth2User>> byRegistrationId;

    public RoutingOAuth2UserService(Oauth2OidcProperties properties) {
        this.byRegistrationId = buildUserServiceMap(properties);
    }

    /**
     * 按本次请求所属客户端取出对应的用户信息服务并委托
     *
     * @param userRequest 用户信息请求
     * @return 身份源用户信息端点返回的原生主体
     * @throws OAuth2AuthenticationException 用户信息请求失败
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        return resolve(userRequest.getClientRegistration().getRegistrationId()).loadUser(userRequest);
    }

    /**
     * 按 registrationId 取出对应的用户信息服务。
     * <p>查不到时返回 {@link #SECURE_FALLBACK} 而不是任一已配置的实例，
     * 使"配置里漏写某个客户端"的后果是走加密通道然后失败，而不是误用了别的客户端的豁免配置。
     *
     * @param registrationId 本次 userinfo 请求所属的客户端
     * @return 对应的用户信息服务，未匹配时为标准证书校验实例
     */
    OAuth2UserService<OAuth2UserRequest, OAuth2User> resolve(String registrationId) {
        return byRegistrationId.getOrDefault(registrationId, SECURE_FALLBACK);
    }

    /**
     * 按客户端配置构造 registrationId 到用户信息服务的映射。
     * 只有显式 ignore-ssl=true 的客户端会得到替换过 RestOperations 的实例，其余一律复用
     * {@link #SECURE_FALLBACK}。
     *
     * @param properties OAuth2/OIDC 配置属性
     * @return registrationId 到用户信息服务的映射
     */
    static Map<String, OAuth2UserService<OAuth2UserRequest, OAuth2User>> buildUserServiceMap(
            Oauth2OidcProperties properties) {
        Map<String, OAuth2UserService<OAuth2UserRequest, OAuth2User>> byRegistrationId = new HashMap<>();
        for (Oauth2OidcRegistrationClientProperties client : properties.getClients()) {
            String registrationId = client.getRegistrationId();
            if (client.isIgnoreSsl()) {
                log.warn("OAuth2 客户端 [{}] 配置了 ignore-ssl=true，其 userinfo 请求将跳过证书与主机名校验，"
                        + "该配置只应用于测试环境", registrationId);
                DefaultOAuth2UserService insecureUserService = new DefaultOAuth2UserService();
                insecureUserService.setRestOperations(new RestOperationBuilder().enableSsl(false).build());
                byRegistrationId.put(registrationId, insecureUserService);
            } else {
                byRegistrationId.put(registrationId, SECURE_FALLBACK);
            }
        }
        return byRegistrationId;
    }
}
