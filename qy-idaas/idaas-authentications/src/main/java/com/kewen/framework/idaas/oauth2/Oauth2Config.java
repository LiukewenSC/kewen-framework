package com.kewen.framework.idaas.oauth2;

import com.kewen.framework.auth.security.config.HttpSecurityCustomizer;
import com.kewen.framework.auth.security.response.SecurityAuthenticationExceptionResolverHandler;
import com.kewen.framework.auth.security.response.SecurityAuthenticationSuccessHandler;
import com.kewen.framework.idaas.oauth2.properties.Oauth2Properties;
import com.kewen.framework.idaas.oauth2.result.Oauth2AuthenticationSuccessResultConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import java.util.List;
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
@EnableConfigurationProperties(Oauth2Properties.class)
public class Oauth2Config implements HttpSecurityCustomizer {

    @Autowired
    private SecurityAuthenticationSuccessHandler successHandler;

    @Autowired
    private SecurityAuthenticationExceptionResolverHandler exceptionResolverHandler;

    @Autowired
    private Oauth2Properties oauth2Properties;

    @Bean
    public AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository() {
        return new HttpSessionOAuth2AuthorizationRequestRepository();
    }
    @Bean
    public Oauth2AuthenticationSuccessResultConverter oauth2JsonSuccessResultConverter(){
        return new Oauth2AuthenticationSuccessResultConverter();
    }
    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
        return new DefaultAuthorizationCodeTokenResponseClient();
    }

    /**
     * 这里自定义了一个DefaultOAuth2UserService，用来解决Oauth2默认的ssl证书校验，如果不需要的话就不用此处的Bean，会默认生成一个
     * @return
     */
    @Bean
    public OAuth2UserService oAuth2UserService() {
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
                    // 配置授权端点
                    .authorizationEndpoint(endpoint -> {
                        endpoint.baseUri(oauth2Properties.getAuthorizationUri())
                                //.authorizationRequestRepository(authorizationRequestRepository())
                                //.authorizationRequestResolver(new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, oauth2Properties.getAuthorizationUri()));
                        ;
                    })
                    // 配置令牌端点
                    .tokenEndpoint(endpoint -> {
                        endpoint.accessTokenResponseClient(accessTokenResponseClient());
                    })
                    .userInfoEndpoint(endpoint -> {
                        endpoint.userService(oAuth2UserService());
                    })
                    .loginProcessingUrl(oauth2Properties.getLoginProcessingUrl())
                    .clientRegistrationRepository(clientRegistrationRepository)
            ;
        });
    }
    private ClientRegistrationRepository clientRegistrationRepository(){
        List<ClientRegistration> registrationList = getClientRegistrations();
        return new InMemoryClientRegistrationRepository(registrationList);
    }



    private List<ClientRegistration> getClientRegistrations() {
        List<ClientRegistration> registrationList = oauth2Properties.getClients().stream().map(
                client -> {
                    ClientRegistration.Builder builder = ClientRegistration.withRegistrationId(client.getRegistrationId());
                    return builder.registrationId(client.getRegistrationId()).clientId(client.getClientId())
                            .clientSecret(client.getClientSecret())
                            .clientAuthenticationMethod(ClientAuthenticationMethod.BASIC)
                            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                            .redirectUriTemplate("{baseUrl}/login/oauth2/code/{registrationId}")
                            .authorizationUri(client.getAuthorizationUri())
                            .userInfoUri(client.getUserInfoUri())
                            .userNameAttributeName(client.getUserNameAttributeName())
                            .scope(client.getScopes())
                            .tokenUri(client.getTokenUri())  // ✅ 修复：使用正确的 tokenUri
                            .build();
                }
        ).collect(Collectors.toList());
        return registrationList;
    }
}