package com.cu.framework.authentication.oauth2;

import com.cu.framework.authentication.config.HttpSecurityCustomizer;
import com.cu.framework.authentication.config.Oauth2ConfiguredCondition;
import com.cu.framework.authentication.response.SecurityAuthenticationExceptionResolverHandler;
import com.cu.framework.authentication.response.SecurityAuthenticationSuccessHandler;
import com.cu.framework.authentication.oauth2.Oauth2OidcProperties.Oauth2OidcRegistrationClientProperties;
import com.cu.framework.authentication.service.SecurityUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
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
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

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
@Conditional(Oauth2ConfiguredCondition.class)
@EnableConfigurationProperties(Oauth2OidcProperties.class)
public class Oauth2Config implements HttpSecurityCustomizer {

    private static final Logger log = LoggerFactory.getLogger(Oauth2Config.class);

    /** OAuth2 登录成功处理器，配置到 oauth2Login 的 successHandler */
    @Autowired
    private SecurityAuthenticationSuccessHandler successHandler;

    /** 认证异常处理器，配置到 oauth2Login 的 failureHandler */
    @Autowired
    private SecurityAuthenticationExceptionResolverHandler exceptionResolverHandler;

    /** OAuth2/OIDC 配置属性（前缀 cu.security.login.oauth2） */
    @Autowired
    private Oauth2OidcProperties oauth2OidcProperties;

    /** 本系统账号来源，用于把身份源认证的账号换成本系统的用户与权限 */
    @Autowired
    private SecurityUserDetailsService securityUserDetailsService;

    /**
     * 授权请求仓库 Bean，基于 HttpSession 保存 OAuth2AuthorizationRequest（含 state 等），供回调时校验
     * @return 基于 Session 的授权请求仓库
     */
    @Bean
    public AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository() {
        return new HttpSessionOAuth2AuthorizationRequestRepository();
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
     * 按 registrationId 分发的 userinfo 用户信息服务，只负责发请求、不负责换主体。
     * <p>本 Bean 同时供两处使用：OAuth2 协议端点用它作原始主体来源（外面再包一层装配），
     * 以及 {@link #oidcUserService()} 内部拉取 userinfo。
     * @return 按客户端配置决定证书校验行为的用户信息服务
     */
    @Bean
    public RoutingOAuth2UserService routingOauth2UserService() {
        return new RoutingOAuth2UserService(oauth2OidcProperties);
    }

    /**
     * OAuth2 协议（非 OIDC）登录端点实际使用的用户信息服务：在 {@link #routingOauth2UserService()}
     * 之上追加一步装配，使进入安全上下文的主体是本框架的统一认证主体而不是协议自带的类型。
     * <p>不注册为 Bean：容器里 {@code OAuth2UserService} 类型的实例保持单一，
     * 装配只在认证链路的末端发生一次。
     * @return 产出统一认证主体的用户信息服务
     */
    private SecurityUserOAuth2UserService securityUserOAuth2UserService() {
        return new SecurityUserOAuth2UserService(routingOauth2UserService(),
                new SsoSecurityUserConverter(securityUserDetailsService));
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
                        endpoint.userService(securityUserOAuth2UserService());
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
     * <p>其 userinfo 拉取复用 {@link #routingOauth2UserService()} 那个<b>只路由不装配</b>的
     * {@link RoutingOAuth2UserService}，因此同样按客户端的 ignore-ssl 决定是否校验证书；
     * 不调用时 OidcUserService 内部默认也是校验的，两者默认行为一致。
     * 这里 MUST NOT 换成 {@link SecurityUserOAuth2UserService}：{@code OidcUserService} 只消费
     * 委托返回主体的声明集，最终主体由它自己用 ID Token 另建，换过去等于每次 OIDC 登录多查
     * 一遍本地账号库，且装配失败会抢在 ID Token 与 UserInfo 的 sub 一致性校验之前抛出。
     * <p>返回的主体由 {@link SecurityUserOidcUserService} 装配成统一认证主体，ID Token 与
     * 声明原样保留在该主体上，依赖 {@code principal instanceof OidcUser} 的会话终止功能不受影响。
     */
    @Bean
    public OidcUserService oidcUserService() {
        SecurityUserOidcUserService oidcUserService =
                new SecurityUserOidcUserService(new SsoSecurityUserConverter(securityUserDetailsService));
        oidcUserService.setOauth2UserService(routingOauth2UserService());
        return oidcUserService;
    }

    /**
     * 通过 OIDC issuer-uri 自动发现创建 ClientRegistration
     * 会自动从 {issuerUri}/.well-known/openid-configuration 获取元数据
     * <p>包内可见是为了让映射字段的接线能被单测覆盖，不是对外扩展点
     */
    ClientRegistration createOidcClientRegistration(Oauth2OidcRegistrationClientProperties client) {
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
        // 不配这一项时主体的标识取 ID Token 的 sub，而 sub 要登录后定位本系统账号，
        // 因此配置了映射字段就必须传给 builder，否则 OIDC 客户端只能用 sub
        Optional.ofNullable(client.getUserNameAttributeName()).ifPresent(builder::userNameAttributeName);
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