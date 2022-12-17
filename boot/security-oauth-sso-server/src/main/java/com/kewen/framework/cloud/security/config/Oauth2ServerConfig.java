package com.kewen.framework.cloud.security.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.InMemoryAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;

/**
 * @descrpition
 * @author kewen
 * @since 2022-12-13 16:57
 */
@Configuration
@EnableAuthorizationServer
@EnableResourceServer
public class Oauth2ServerConfig implements AuthorizationServerConfigurer {

    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    ClientDetailsService clientDetailsService;

    /**
     * token 存储仓库，存放生成的token
     *
     * @return
     */
    @Bean
    public TokenStore tokenStore() {
        InMemoryTokenStore tokenStore = new InMemoryTokenStore();
        System.out.println(tokenStore.getAccessTokenCount());
        return tokenStore;
    }

    /**
     * @return
     */
    @Bean
    AuthorizationCodeServices authorizationCodeServices() {
        return new InMemoryAuthorizationCodeServices();
    }

    /**
     * 授权服务
     *
     * @return
     */
    @Bean
    public AuthorizationServerTokenServices authorizationServerTokenServices() {
        DefaultTokenServices tokenServices = new DefaultTokenServices();
        tokenServices.setClientDetailsService(clientDetailsService);
        tokenServices.setSupportRefreshToken(true);
        tokenServices.setTokenStore(tokenStore());
        tokenServices.setAccessTokenValiditySeconds(2 * 60 * 60);
        tokenServices.setRefreshTokenValiditySeconds(3 * 24 * 60 * 60);
        return tokenServices;
    }

    /**
     * 配置令牌的安全约束
     *
     * @param security
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security.checkTokenAccess("permitAll()")
                .allowFormAuthenticationForClients();
    }

    /**
     * 配置客户端的详细信息
     *
     * @param clients
     * @throws Exception
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
                .withClient("client")
                .secret(passwordEncoder.encode("123"))
                .resourceIds("res1", "res2")
                .authorizedGrantTypes("authorization_code", "refresh_token")
                .scopes("all")
                .redirectUris("http://localhost:8082/authCallback")
                .and()
                .withClient("res-client")
                .secret(passwordEncoder.encode("456"))
                .and()
                .withClient("client1")
                .secret(passwordEncoder.encode("secret1"))
                //自动允许
                .autoApprove(true)
                .authorizedGrantTypes("authorization_code", "refresh_token")
                .scopes("all")
                .redirectUris("http://localhost:8001/login")
                .and()
                .withClient("client2")
                .secret(passwordEncoder.encode("secret2"))
                .autoApprove(true)
                .authorizedGrantTypes("authorization_code", "refresh_token")
                .redirectUris("http://localhost:8002/login")
                .scopes("all")
        ;
    }

    /**
     * 配置令牌的访问端点和服务
     *
     * @param endpoints
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints
                .authorizationCodeServices(authorizationCodeServices()) // 配置授权码的存储
                .tokenServices(authorizationServerTokenServices());     // 配置令牌的存储
    }
}


