package com.kewen.framework.idaas.oauth2;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.util.ArrayList;
import java.util.List;

/**
 * 自定义 OAuth2 用户服务
 * 支持处理非标准格式的 Userinfo 响应（如 HTML 错误页面）
 *
 * @author kewen
 * @since 1.0
 */
public class RestOperationBuilder {

    private static final Logger log = LoggerFactory.getLogger(RestOperationBuilder.class);

    private boolean enableSsl = true;

    public RestOperationBuilder enableSsl(boolean enableSsl) {
        this.enableSsl = enableSsl;
        return this;
    }

    public RestOperations build() {
        return createRestOperations();
    }

    /**
     * 创建自定义的 RestOperations
     */
    private RestOperations createRestOperations() {
        RestTemplate restTemplate = new RestTemplate();

        // 配置错误处理器
        restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());

        // 配置消息转换器
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        messageConverters.add(new StringHttpMessageConverter());
        messageConverters.add(new MappingJackson2HttpMessageConverter());
        restTemplate.setMessageConverters(messageConverters);

        // 启用重定向跟随（默认已启用，但这里明确配置）

        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        httpClientBuilder.setRedirectStrategy(new LaxRedirectStrategy());
        if (!enableSsl) {
            addSsl(httpClientBuilder);
        }
        HttpClient httpClient = httpClientBuilder.build();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        restTemplate.setRequestFactory(factory);

        return restTemplate;
    }

    private void addSsl(HttpClientBuilder httpClientBuilder) {
        // 创建信任所有证书的 SSLContext（仅用于开发环境）
        try {
            SSLContext sslContext = SSLContextBuilder.create()
                    .loadTrustMaterial((chain, authType) -> true) // 信任所有证书
                    .build();

            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                    sslContext,
                    NoopHostnameVerifier.INSTANCE // 忽略主机名验证
            );
            httpClientBuilder.setSSLSocketFactory(sslSocketFactory);
        } catch (Exception e) {
            log.warn("Failed to configure SSL context, using default configuration. Error: {}", e.getMessage());
            // 如果配置失败，使用默认的 RestTemplate 配置
        }
    }
}
