package com.cu.framework.idaas.oauth2;

import com.cu.framework.auth.security.config.HttpSecurityCustomizer;
import com.cu.framework.auth.security.response.SecurityAuthenticationExceptionResolverHandler;
import com.cu.framework.auth.security.response.SecurityAuthenticationSuccessHandler;
import com.cu.framework.idaas.oauth2.Oauth2OidcProperties.Oauth2OidcRegistrationClientProperties;
import com.cu.framework.idaas.oauth2.result.Oauth2AuthenticationSuccessResultConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ClientRegistrations;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;
import java.util.stream.Collectors;

/**
 * OAuth2 认证配置
 * <p>
 * 通过实现 HttpSecurityCustomizer 接口，将 OAuth2 登录配置注入到 Spring Security 过滤器链中。
 * 支持多种 OAuth2 提供商（如 GitHub、Google、自定义 OAuth2 服务器等）
 *
 * @author kewen
 * @since 4.24.0-SP01
 */
@Configuration
@EnableConfigurationProperties(Oauth2OidcProperties.class)
public class Oauth2Config implements HttpSecurityCustomizer {

    private static final Logger log = LoggerFactory.getLogger(Oauth2Config.class);

    /**
     * 执行标准证书校验的用户信息服务。
     * 既作为 ignore-ssl 未开启的客户端的默认实现，也作为 registrationId 查不到配置时的兜底，
     * 使配置缺失时的失败方向是"登录不上"而不是"悄悄不校验证书"
     */
    static final OAuth2UserService<OAuth2UserRequest, OAuth2User> SECURE_FALLBACK =
            new DefaultOAuth2UserService();

    /** OAuth2 登录成功处理器，配置到 oauth2Login 的 successHandler */
    @Autowired
    private SecurityAuthenticationSuccessHandler successHandler;

    /** 认证异常处理器，配置到 oauth2Login 的 failureHandler */
    @Autowired
    private SecurityAuthenticationExceptionResolverHandler exceptionResolverHandler;

    /** OAuth2/OIDC 配置属性（前缀 cu.security.login.oauth2） */
    @Autowired
    private Oauth2OidcProperties oauth2OidcProperties;

    /**
     * 授权请求仓库 Bean，基于 HttpSession 保存 OAuth2AuthorizationRequest（含 state 等），供回调时校验
     * @return 基于 Session 的授权请求仓库
     */
    @Bean
    public AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository() {
        return new HttpSessionOAuth2AuthorizationRequestRepository();
    }

    /**
     * OAuth2 登录成功结果转换器 Bean，把 OAuth2User 属性转换为统一的登录成功响应
     * @return OAuth2 认证成功结果转换器
     */
    @Bean
    public Oauth2AuthenticationSuccessResultConverter oauth2JsonSuccessResultConverter(){
        return new Oauth2AuthenticationSuccessResultConverter();
    }

    /**
     * 实际这里可以不配，默认会有
     * @return
     */
    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
        return new DefaultAuthorizationCodeTokenResponseClient();
    }

    /**
     * 按 registrationId 分发的 userinfo 用户信息服务。
     * <p>是否跳过 SSL 证书校验由每个客户端自己的 {@code ignore-ssl} 决定，默认执行标准校验。
     * <p>之所以按 registrationId 查表而不是用 {@code DelegatingOAuth2UserService}：本仓库依赖的
     * Spring Security 5.7.11 中 {@code OAuth2UserService} 没有 supports() 方法，而
     * {@code DelegatingOAuth2UserService.loadUser} 会对列表里每个 delegate 都调用一次 loadUser，
     * 等于向每个 IDP 各发一次 userinfo 请求。
     * <p>{@code RestOperationBuilder} 的作用不止 SSL：它还替换了消息转换器与错误处理器，
     * 用于容忍部分 IDP 返回的非标准 userinfo 响应（如 HTML 错误页）。
     * 其来历见 docs/说明文档/已废弃实现与决策记录.md 第二节。
     * @return 按客户端配置决定证书校验行为的用户信息服务
     */
    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
        Map<String, OAuth2UserService<OAuth2UserRequest, OAuth2User>> byRegistrationIdMap = buildUserServiceMap();
        return userRequest -> resolveUserService(byRegistrationIdMap,
                        userRequest.getClientRegistration().getRegistrationId())
                .loadUser(userRequest);
    }

    /**
     * 按 registrationId 取出对应的用户信息服务。
     * <p>查不到时返回 {@link #SECURE_FALLBACK} 而不是任一已配置的实例，
     * 使"配置里漏写某个客户端"的后果是走加密通道然后失败，而不是误用了别的客户端的豁免配置。
     * @param byRegistrationId registrationId 到用户信息服务的映射
     * @param registrationId 本次 userinfo 请求所属的客户端
     * @return 对应的用户信息服务，未匹配时为标准证书校验实例
     */
    static OAuth2UserService<OAuth2UserRequest, OAuth2User> resolveUserService(
            Map<String, OAuth2UserService<OAuth2UserRequest, OAuth2User>> byRegistrationId,
            String registrationId) {
        return byRegistrationId.getOrDefault(registrationId, SECURE_FALLBACK);
    }

    /**
     * 按客户端配置构造 registrationId 到用户信息服务的映射。
     * 只有显式 ignore-ssl=true 的客户端会得到替换过 RestOperations 的实例，其余一律复用
     * {@link #SECURE_FALLBACK}。
     * @return registrationId 到用户信息服务的映射
     */
    Map<String, OAuth2UserService<OAuth2UserRequest, OAuth2User>> buildUserServiceMap() {
        Map<String, OAuth2UserService<OAuth2UserRequest, OAuth2User>> byRegistrationId = new HashMap<>();
        for (Oauth2OidcRegistrationClientProperties client : oauth2OidcProperties.getClients()) {
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

    /**
     * 定制 Spring Security 过滤器链的 oauth2Login 配置：设置登录成功/失败处理器、
     * 授权端点 baseUri、令牌端点的 accessTokenResponseClient、用户信息端点的
     * userService 与 oidcUserService、登录回调地址及客户端注册仓库
     * @param http Spring Security 的 HttpSecurity 构建器
     * @throws Exception 配置过滤器链失败时抛出
     */
    @Override
    public void customizer(HttpSecurity http) throws Exception {
        ClientRegistrationRepository clientRegistrationRepository = clientRegistrationRepository();
        http.oauth2Login(oauth2 -> {
            oauth2
                    // 配置成功处理器
                    .successHandler(successHandler)
                    // 配置失败处理器
                    .failureHandler(exceptionResolverHandler)
                    // 配置本地单点登录端点
                    .authorizationEndpoint(endpoint -> {
                        endpoint.baseUri(oauth2OidcProperties.getSsoAuthorizationUri())
                                //存 OAuth2AuthorizationRequest（包含 state、nonce 等）到 session，用于回调时校验 CSRF。
                                // HttpSessionOAuth2AuthorizationRequestRepository 本来就是默认实现不加也一样。
                                //.authorizationRequestRepository(authorizationRequestRepository())

                                // 显式指定用哪个 ClientRegistrationRepository 和 baseUri 来解析请求路径中的 registrationId。
                                // 只有当你需要自定义请求构建逻辑（比如往授权请求里加额外参数、改 state 生成方式）时，才需要自己实现并传入 authorizationRequestResolver。
                                // 否则就用默认的。
                                //.authorizationRequestResolver(new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, oauth2OidcProperties.getSsoAuthorizationUri()));
                        ;
                    })
                    // 配置令牌请求配置，配置的是令牌交换请求用什么 HTTP 客户端去发。默认可以不配，我们这里也可以不配
                    .tokenEndpoint(endpoint -> {
                        endpoint.accessTokenResponseClient(accessTokenResponseClient());
                    })
                    //userinfo配置 配置的是拿到 token 后怎么去获取用户信息（用什么 HTTP 客户端、是否校验 SSL 等）
                    .userInfoEndpoint(endpoint -> {
                        endpoint.userService(oauth2UserService());
                        endpoint.oidcUserService(oidcUserService());
                    })
                    .loginProcessingUrl(oauth2OidcProperties.getLoginProcessingUrl())
                    .clientRegistrationRepository(clientRegistrationRepository)
            ;
        });
    }
    /**
     * 根据配置的 clients 列表构建内存版客户端注册仓库：
     * protocolType 为 OAUTH2 时按手动配置的端点创建，为 OIDC 时按 issuerUri 自动发现创建，
     * 其他类型记录告警日志并跳过
     * @return 客户端注册仓库
     */
    private ClientRegistrationRepository clientRegistrationRepository(){
        List<ClientRegistration> registrationList = oauth2OidcProperties.getClients().stream().map(
                client -> {
                    switch (client.getProtocolType()){
                        case OAUTH2:
                            return createOAuth2ClientRegistration(client);
                        case OIDC:
                            return createOidcClientRegistration(client);
                        default:
                            log.warn("oauth protocol type error : "+client.getProtocolType());
                    }
                    return null;
                }
        ).filter(Objects::nonNull).collect(Collectors.toList());
        return new InMemoryClientRegistrationRepository(registrationList);
    }


    /**
     * 通过手动配置的端点创建 OAuth2 ClientRegistration
     */
    private ClientRegistration createOAuth2ClientRegistration(Oauth2OidcRegistrationClientProperties client) {
        ClientRegistration.Builder builder = ClientRegistration.withRegistrationId(client.getRegistrationId())
                .clientId(client.getClientId())
                .clientSecret(client.getClientSecret())
                .clientAuthenticationMethod(client.getClientAuthenticationMethod())
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(client.getRedirectUriTemplate())
                .scope(client.getScopes());

        // 可选配置项：仅在非空时设置（有 discoveryUri 时会自动发现）
        Optional.ofNullable(client.getAuthorizationUri()).ifPresent(builder::authorizationUri);
        Optional.ofNullable(client.getTokenUri()).ifPresent(builder::tokenUri);
        Optional.ofNullable(client.getUserInfoUri()).ifPresent(builder::userInfoUri);
        Optional.ofNullable(client.getUserNameAttributeName()).ifPresent(builder::userNameAttributeName);

        return builder.build();
    }




    /** ---------------------------------------oidc配置------------------------------------------- **/

    /**
     * 创建 OIDC UserService 用于处理 OIDC 协议的用户信息获取及 ID Token 验证。
     * <p>其 userinfo 拉取复用 {@link #oauth2UserService()}，因此同样按客户端的 ignore-ssl
     * 决定是否校验证书；不调用时 OidcUserService 内部默认也是校验的，两者默认行为一致。
     */
    @Bean
    public OidcUserService oidcUserService() {
        OidcUserService oidcUserService = new OidcUserService();
        oidcUserService.setOauth2UserService(oauth2UserService());
        return oidcUserService;
    }

    /**
     * 通过 OIDC issuer-uri 自动发现创建 ClientRegistration
     * 会自动从 {issuerUri}/.well-known/openid-configuration 获取元数据
     */
    private ClientRegistration createOidcClientRegistration(Oauth2OidcRegistrationClientProperties client) {
        // 确保 OIDC 协议必需的 openid scope
        Set<String> scopes = client.getScopes();
        if (scopes == null || scopes.isEmpty()) {
            scopes = Collections.singleton("openid");
        } else if (!scopes.contains("openid")) {
            scopes = new HashSet<>(scopes);
            scopes.add("openid");
        }

        ClientRegistration.Builder builder = ClientRegistrations.fromIssuerLocation(client.getIssuerUri())
                .registrationId(client.getRegistrationId())
                .clientId(client.getClientId())
                .clientSecret(client.getClientSecret())
                .clientAuthenticationMethod(client.getClientAuthenticationMethod())
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(client.getRedirectUriTemplate())
                .scope(scopes.toArray(new String[0]))
                .clientName(client.getClientName());
        
        // 可选配置项：JWK Set URI（有 discoveryUri 时会自动发现）
        Optional.ofNullable(client.getJwkSetUri()).ifPresent(builder::jwkSetUri);
        Optional.ofNullable(client.getAuthorizationUri()).ifPresent(builder::authorizationUri);
        Optional.ofNullable(client.getTokenUri()).ifPresent(builder::tokenUri);
        Optional.ofNullable(client.getUserInfoUri()).ifPresent(builder::userInfoUri);
        return builder.build();
    }

    /**
     * OIDC RP-Initiated Logout 处理器
     * <p>
     * 如果 OIDC Provider 支持 RP-Initiated Logout，Spring Security 会自动处理登出。
     * 该处理器支持在登出后重定向到指定的 postLogoutRedirectUri。
     * </p>
     *
     * @return OIDC 登出成功处理器
     */
    @Bean
    @ConditionalOnMissingBean
    public OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler() {
        OidcClientInitiatedLogoutSuccessHandler handler =
                new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository());
        handler.setPostLogoutRedirectUri("{baseUrl}");
        return handler;
    }

}