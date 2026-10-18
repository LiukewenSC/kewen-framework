package com.cu.framework.authentication.oauth2;

import com.cu.framework.authentication.oauth2.Oauth2OidcProperties.Oauth2OidcRegistrationClientProperties;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.oauth2.client.registration.ClientRegistration;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpServer;

/**
 * OIDC 客户端注册项的接线：映射字段必须真的传进 ClientRegistration
 *
 * @author kewen
 * @since 2026-10-18
 */
public class OidcClientRegistrationTest {

    /**
     * 用本地一次性发现端点顶替真实身份源，只验接线不发外网请求
     */
    private HttpServer startDiscoveryServer() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            String issuer = "http://" + exchange.getRequestHeaders().getFirst("Host");
            String body = "{\"issuer\":\"" + issuer + "\","
                    + "\"authorization_endpoint\":\"" + issuer + "/oauth2/authorize\","
                    + "\"token_endpoint\":\"" + issuer + "/oauth2/token\","
                    + "\"jwks_uri\":\"" + issuer + "/.well-known/jwks.json\","
                    + "\"userinfo_endpoint\":\"" + issuer + "/oidc/userinfo\","
                    + "\"response_types_supported\":[\"code\"],"
                    + "\"subject_types_supported\":[\"public\"],"
                    + "\"id_token_signing_alg_values_supported\":[\"RS256\"]}";
            exchange.getResponseHeaders().add("Content-Type", "application/json;charset=UTF-8");
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(bytes);
            }
        });
        server.start();
        return server;
    }

    private Oauth2OidcRegistrationClientProperties oidcClient(String issuerUri, String userNameAttributeName) {
        Oauth2OidcRegistrationClientProperties client = new Oauth2OidcRegistrationClientProperties();
        client.setProtocolType(OauthProtocolType.OIDC);
        client.setRegistrationId("demo-oidc");
        client.setClientId("client-id");
        client.setClientSecret("client-secret");
        client.setClientName("演示 OIDC");
        client.setIssuerUri(issuerUri);
        client.setUserNameAttributeName(userNameAttributeName);
        return client;
    }

    /**
     * 配了映射字段就按该字段取标识，登录后据此定位本地账号
     */
    @Test
    public void configuredClaimIsAppliedToOidcRegistration() throws Exception {
        HttpServer server = startDiscoveryServer();
        try {
            String issuerUri = "http://localhost:" + server.getAddress().getPort();
            ClientRegistration registration = new Oauth2Config()
                    .createOidcClientRegistration(oidcClient(issuerUri, "preferred_username"));

            Assert.assertEquals("preferred_username",
                    registration.getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName());
        } finally {
            server.stop(0);
        }
    }

    /**
     * 没配映射字段时沿用 OIDC 的默认主体标识 sub
     */
    @Test
    public void absentClaimFallsBackToSub() throws Exception {
        HttpServer server = startDiscoveryServer();
        try {
            String issuerUri = "http://localhost:" + server.getAddress().getPort();
            ClientRegistration registration = new Oauth2Config()
                    .createOidcClientRegistration(oidcClient(issuerUri, null));

            Assert.assertEquals("sub",
                    registration.getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName());
        } finally {
            server.stop(0);
        }
    }
}
