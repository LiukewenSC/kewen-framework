package com.cu.framework.authentication.oauth2;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.LaxRedirectStrategy;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
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

    /** 是否启用 SSL 证书校验，默认 true；设为 false 时构建的客户端信任所有证书并跳过主机名校验 */
    private boolean enableSsl = true;

    /**
     * 设置是否启用 SSL 证书校验（链式调用）
     * @param enableSsl false 表示跳过证书与主机名校验
     * @return 当前构建器
     */
    public RestOperationBuilder enableSsl(boolean enableSsl) {
        this.enableSsl = enableSsl;
        return this;
    }

    /**
     * 构建 RestOperations
     * @return 按当前配置创建的 RestOperations
     */
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
        messageConverters.add(new JacksonJsonHttpMessageConverter());
        restTemplate.setMessageConverters(messageConverters);

        // 启用重定向跟随（默认已启用，但这里明确配置）
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        httpClientBuilder.setRedirectStrategy(new LaxRedirectStrategy());
        if (!enableSsl) {
            PoolingHttpClientConnectionManager connectionManager = createTrustAllConnectionManager();
            if (connectionManager != null) {
                httpClientBuilder.setConnectionManager(connectionManager);
            }
        }
        HttpClient httpClient = httpClientBuilder.build();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        restTemplate.setRequestFactory(factory);

        return restTemplate;
    }

    /**
     * 创建信任所有证书、忽略主机名校验的连接管理器（仅用于开发环境）；
     * 配置失败时记录告警并返回 null，调用方沿用默认 SSL 配置
     */
    private PoolingHttpClientConnectionManager createTrustAllConnectionManager() {
        try {
            SSLContext sslContext = SSLContextBuilder.create()
                    .loadTrustMaterial((chain, authType) -> true) // 信任所有证书
                    .build();

            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                    sslContext,
                    NoopHostnameVerifier.INSTANCE // 忽略主机名验证
            );
            return PoolingHttpClientConnectionManagerBuilder.create()
                    .setSSLSocketFactory(sslSocketFactory)
                    .build();
        } catch (Exception e) {
            log.warn("Failed to configure SSL context, using default configuration. Error: {}", e.getMessage());
            return null;
        }
    }
}
