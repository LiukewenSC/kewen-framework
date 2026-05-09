package com.kewen.framework.idaas.oauth2;

import com.kewen.framework.auth.security.config.HttpSecurityCustomizer;
import com.kewen.framework.auth.security.response.SecurityAuthenticationExceptionResolverHandler;
import com.kewen.framework.auth.security.response.SecurityAuthenticationSuccessHandler;
import com.kewen.framework.idaas.oauth2.Oauth2OidcProperties.Oauth2OidcRegistrationClientProperties;
import com.kewen.framework.idaas.oauth2.result.Oauth2AuthenticationSuccessResultConverter;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

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
    @Autowired
    private SecurityAuthenticationSuccessHandler successHandler;

    @Autowired
    private SecurityAuthenticationExceptionResolverHandler exceptionResolverHandler;

    @Autowired
    private Oauth2OidcProperties oauth2OidcProperties;

    @Bean
    public AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository() {
        return new HttpSessionOAuth2AuthorizationRequestRepository();
    }
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
     * 这里自定义了一个DefaultOAuth2UserService，用来解决Oauth2默认的ssl证书校验，如果不需要的话就不用此处的Bean，会默认生成一个
     * @return
     */
    @Bean
    public OAuth2UserService ignoreSslOAuth2UserService() {
        //return new IdaasSPOAuth2UserService();
        DefaultOAuth2UserService defaultOAuth2UserService = new DefaultOAuth2UserService();
        defaultOAuth2UserService.setRestOperations(new RestOperationBuilder().enableSsl(false).build());
        return defaultOAuth2UserService;
    }

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
                        endpoint.userService(ignoreSslOAuth2UserService());
                        endpoint.oidcUserService(oidcUserService());
                    })
                    .loginProcessingUrl(oauth2OidcProperties.getLoginProcessingUrl())
                    .clientRegistrationRepository(clientRegistrationRepository)
            ;
        });
    }
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
        ClientRegistration.Builder builder = ClientRegistration.withRegistrationId(client.getRegistrationId());
        return builder.registrationId(client.getRegistrationId()).clientId(client.getClientId())
                .clientSecret(client.getClientSecret())
                .clientAuthenticationMethod(client.getClientAuthenticationMethod())
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(client.getRedirectUriTemplate())
                .authorizationUri(client.getAuthorizationUri())
                .userInfoUri(client.getUserInfoUri())
                .userNameAttributeName(client.getUserNameAttributeName())
                .scope(client.getScopes())
                .tokenUri(client.getTokenUri())
                .build();
    }




    /** ---------------------------------------oidc配置------------------------------------------- **/

    /**
     * 创建 OIDC UserService 用于处理 OIDC 协议的用户信息获取及 ID Token 验证
     * 支持自定义 SSL 校验（用于测试环境）
     */
    @Bean
    public OidcUserService oidcUserService() {
        OidcUserService oidcUserService = new OidcUserService();
        oidcUserService.setOauth2UserService(ignoreSslOAuth2UserService());
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
        if (StringUtils.isNotBlank(client.getJwkSetUri())){
            builder.jwkSetUri(client.getJwkSetUri());
        }
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