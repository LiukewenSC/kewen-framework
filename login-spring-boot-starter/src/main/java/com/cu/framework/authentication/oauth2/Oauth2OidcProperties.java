package com.cu.framework.authentication.oauth2;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.oauth2.core.AuthenticationMethod;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import java.util.*;

/**
 * OAuth2 / OIDC 统一配置属性
 * <p>
 * 支持 OAuth2 和 OIDC 两种协议的客户端注册配置。
 * <ul>
 *   <li><b>OAuth2 模式</b>：手动指定 authorizationUri / tokenUri / userInfoUri 等端点</li>
 *   <li><b>OIDC 模式</b>：通过 oidcDiscoveryUri 自动发现所有端点（推荐）</li>
 * </ul>
 *
 * @author kewen
 * @since 1.0
 */
@ConfigurationProperties("cu.security.login.oauth2")
@Data
public class Oauth2OidcProperties {

    /**
     * 本地 OAuth2/OIDC 登录授权端点的基础 URI
     * <p><b>协议归属：</b> OAuth2通用 + OIDC通用</p>
     * <p>用于发起认证请求的端点路径，用户访问此路径后会重定向到 IDP 进行登录。</p>
     * <p>默认值：{@code /oauth2/authorization}，与 Spring Security 官方
     * {@code OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI} 保持一致。</p>
     * <p>访问示例：{@code /oauth2/authorization/{registrationId}}</p>
     */
    private String ssoAuthorizationUri = "/oauth2/authorization";

    /**
     * OAuth2/OIDC 登录回调处理端点
     * <p><b>协议归属：</b> OAuth2通用 + OIDC通用</p>
     * <p>IDP 认证完成后重定向回本应用的端点，由 {@code OAuth2LoginAuthenticationFilter} 处理。</p>
     * <p>默认值：{@code /login/oauth2/code/{registrationId}}</p>
     */
    private String loginProcessingUrl = OAuth2LoginAuthenticationFilter.DEFAULT_FILTER_PROCESSES_URI;

    /**
     * OAuth2/OIDC 客户端注册配置列表
     * <p><b>协议归属：</b> OAuth2通用 + OIDC通用</p>
     * <p>支持配置多个 IDP 提供商，每个客户端需指定 registrationId 作为唯一标识。</p>
     */
    private List<Oauth2OidcRegistrationClientProperties> clients ;

    /**
     * 单个 OAuth2/OIDC 客户端的注册配置，对应 cu.security.login.oauth2.clients 列表元素
     * @author kewen
     * @since 2026-05-10
     */
    @Data
    public static class Oauth2OidcRegistrationClientProperties {


        /**
         * 协议类型，表明是Oauth2还是Oidc，默认为Oauth2
         */
        private OauthProtocolType protocolType = OauthProtocolType.OAUTH2;

        /**
         * 客户端注册 ID（唯一标识）
         * <p><b>协议归属：</b> OAuth2通用 + OIDC通用</p>
         * <p>每个 IDP 提供商的唯一标识，用于区分不同的身份源。
         * 在认证 URL 中会使用此 ID，例如：{@code /oauth2/authorization/{registrationId}}。</p>
         * <p><b>必填</b></p>
         */
        private String registrationId;

        /**
         * 客户端 ID
         * <p><b>协议归属：</b> OAuth2通用 + OIDC通用</p>
         * <p>由 IDP 分配的客户端标识。
         * <b>必填</b></p>
         */
        private String clientId;

        /**
         * 客户端密钥
         * <p><b>协议归属：</b> OAuth2通用 + OIDC通用</p>
         * <p>由 IDP 分配的客户端密钥，用于在令牌端点进行身份验证。
         * <b>必填</b></p>
         */
        private String clientSecret;

        /**
         * 客户端认证方式
         * <p><b>协议归属：</b> OAuth2通用 + OIDC通用</p>
         * <p>客户端向令牌端点发起请求时的认证方式，可选值：</p>
         * <ul>
         *   <li>{@code client_secret_basic}（默认）- HTTP Basic 认证（推荐）</li>
         *   <li>{@code post} - 表单提交 client_id + client_secret</li>
         *   <li>{@code none} - 无需认证（仅适用于 public client）</li>
         * </ul>
         */
        private ClientAuthenticationMethod clientAuthenticationMethod = ClientAuthenticationMethod.CLIENT_SECRET_BASIC;

        /**
         * 授权类型
         * <p><b>协议归属：</b> OAuth2通用 + OIDC通用</p>
         * <p>目前仅支持授权码模式（{@code authorization_code}）。
         * 其他授权类型（如 implicit、client_credentials）不支持。</p>
         */
        private AuthorizationGrantType authorizationGrantType;

        /**
         * 回调地址模板
         * <p><b>协议归属：</b> OAuth2通用 + OIDC通用</p>
         * <p>IDP 认证成功后重定向的地址模板。
         * 默认值：{@code {baseUrl}/login/oauth2/code/{registrationId}}</p>
         * <p>变量说明：</p>
         * <ul>
         *   <li>{@code {baseUrl}} - 当前应用的根 URL（自动适配）</li>
         *   <li>{@code {registrationId}} - 当前客户端的 registrationId</li>
         * </ul>
         */
        private String redirectUriTemplate = "{baseUrl}/login/oauth2/code/{registrationId}";

        /**
         * 请求的权限范围（Scopes）
         * <p><b>协议归属：</b> OAuth2通用 + OIDC通用</p>
         * <p>向 IDP 申请的权限范围列表。</p>
         * <ul>
         *   <li><b>OAuth2 场景</b>：如 {@code read}、{@code write}、{@code profile}、{@code email} 等</li>
         *   <li><b>OIDC 场景</b>：<b>必须包含 {@code openid}</b>，系统会自动添加该 scope（即使未配置）</li>
         * </ul>
         */
        private Set<String> scopes;

        /**
         * OIDC/OAuth2 自动发现端点（Issuer URI）
         * <p>配置后，系统会自动从
         * {@code {discoveryUri}/.well-known/openid-configuration} 或
         * {@code {discoveryUri}/.well-known/oauth-authorization-server} 发现所有端点元数据。</p>
         * <p><b>使用规则：</b></p>
         * <ul>
         *   <li>配置此项后，可省略 {@code authorizationUri}、{@code tokenUri}、{@code userInfoUri} 等手动配置</li>
         *   <li>未配置时，使用手动指定的端点（传统 OAuth2 模式）</li>
         *   <li>手动配置会覆盖自动发现的配置</li>
         * </ul>
         * <p>示例：{@code https://your-idp.example.com}</p>
         */
        private String issuerUri;

        /**
         * 授权端点 URL
         * <p><b>协议归属：</b> <b>OAuth2 手动配置专用</b>（OIDC 模式下由自动发现获取）</p>
         * <p>IDP 用于用户认证的授权端点。
         * 当 <b>未配置</b> {@code oidcDiscoveryUri} 时，此项为 <b>必填</b>。</p>
         * <p>示例：{@code https://idp.example.com/oauth2/authorize}</p>
         */
        private String authorizationUri;

        /**
         * 令牌端点 URL
         * <p><b>协议归属：</b> <b>OAuth2 手动配置专用</b>（OIDC 模式下由自动发现获取）</p>
         * <p>IDP 用于交换 access_token 的令牌端点。
         * 当 <b>未配置</b> {@code oidcDiscoveryUri} 时，此项为 <b>必填</b>。</p>
         * <p>示例：{@code https://idp.example.com/oauth2/token}</p>
         */
        private String tokenUri;

        /**
         * 用户信息端点 URL
         * <p><b>协议归属：</b> <b>OAuth2 手动配置专用</b>（OIDC 模式下由自动发现获取）</p>
         * <p>IDP 用于获取已认证用户详细信息的端点。
         * 当 <b>未配置</b> {@code oidcDiscoveryUri} 时，此项为 <b>必填</b>。</p>
         * <p>示例：{@code https://idp.example.com/userinfo}</p>
         */
        private String userInfoUri;

        /**
         * 用户信息端点认证方式
         * <p><b>协议归属：</b> <b>OAuth2 手动配置专用</b>（OIDC 模式下由自动发现获取）</p>
         * <p>访问用户信息端点时的认证方式：</p>
         * <ul>
         *   <li>{@code header}（默认）- 在请求头中携带 access_token（Bearer Token）</li>
         *   <li>{@code form} - 通过表单参数提交 access_token</li>
         *   <li>{@code query} - 通过查询参数提交 access_token</li>
         * </ul>
         */
        private AuthenticationMethod userInfoAuthenticationMethod = AuthenticationMethod.HEADER;

        /**
         * 用户名属性名称
         * <p><b>协议归属：</b> OAuth2 与 OIDC 通用
         * <p>用户信息响应中指向本系统用户名的属性名。身份源只负责证明"这个人是谁"，
         * 登录成功后系统用该属性的值作为用户名向 {@code SecurityUserDetailsService} 取本地账号与权限；
         * <b>值对不上本地账号时本次登录直接失败</b>（错误码 {@code sso_local_user_not_found}）。
         * <ul>
         *   <li><b>OAuth2 场景</b>：按 IDP 返回的用户信息字段指定，如 {@code login}（GitHub）、{@code username}、{@code email}</li>
         *   <li><b>OIDC 场景</b>：常用 {@code preferred_username}；不配置时回退 OIDC 标准主体标识 {@code sub}，
         *       而 {@code sub} 通常是身份源侧的数字 ID 或配对码，等于本系统用户名的情况很少</li>
         * </ul>
         * <p>想按身份源的其他字段（如邮箱）匹配本地账号，应由业务实现的
         * {@code SecurityUserDetailsService} 承担，本项只决定传给它的那个值。
         */
        private String userNameAttributeName;

        /**
         * JWK Set 端点 URL
         * <p><b>协议归属：</b> OAuth2通用 + OIDC通用（OIDC 模式下自动发现）</p>
         * <p>用于获取 JSON Web Key Set 的端点，主要用于验证 ID Token（JWT）的签名。</p>
         * <ul>
         *   <li><b>OIDC 模式</b>：由 {@code oidcDiscoveryUri} 自动发现，无需手动配置</li>
         *   <li><b>OAuth2 手动模式</b>：可选配置，用于支持 JWT 格式的 access_token 验证</li>
         * </ul>
         * <p>示例：{@code https://idp.example.com/.well-known/jwks.json}</p>
         */
        private String jwkSetUri;

        /**
         * 客户端显示名称
         * <p><b>协议归属：</b> OAuth2通用 + OIDC通用</p>
         * <p>用于日志输出和调试的可读名称，非必填。</p>
         */
        private String clientName;

        /**
         * 是否忽略 SSL 证书校验
         * <p><b>协议归属：</b> OAuth2通用 + OIDC通用</p>
         * <p>设置为 {@code true} 时会跳过对 IDP 的 SSL 证书与主机名校验（<b>仅用于测试/开发环境</b>）。</p>
         * <p><b>⚠️ 生产环境务必保持默认值 {@code false}</b>，即执行证书校验。</p>
         * <p><b>作用范围：</b>只影响 userinfo 端点。OIDC Discovery 与 JWK Set 拉取走 Spring 自带的
         * HTTP 客户端，始终执行标准证书校验，配 {@code true} 也不会放宽——若失败发生在 Discovery 阶段，
         * 应把 IDP 证书导入 JVM 信任库。</p>
         */
        private boolean ignoreSsl = false;
    }
}
