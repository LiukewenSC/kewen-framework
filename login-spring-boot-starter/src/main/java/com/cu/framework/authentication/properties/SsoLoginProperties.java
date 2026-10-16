package com.cu.framework.authentication.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 单点登录（OAuth2 / OIDC / SAML2）三类协议共用的登录属性
 *
 * @author kewen
 * @since 2026-10-16
 */
@ConfigurationProperties("cu.security.login.sso")
@Data
public class SsoLoginProperties {

    /**
     * 单点登录流程结束后浏览器回跳的前端地址，取值来自配置，无默认值。
     * 留空时三类协议的成功与失败响应都保持 JSON 输出，行为与该属性引入之前一致。
     * 写相对地址时由容器按当前请求的 scheme/host/port 解析，写绝对地址则原样使用，
     * 因此同一项同时适用于同源页面与独立部署的前端。
     */
    private String redirectUrl;
}
