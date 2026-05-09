package com.kewen.framework.idaas.oauth2;

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
@ConfigurationProperties("kewen.security.login.oauth2")
@Data
public class Oauth2OidcProperties {

    /**
     * 本地 OAuth2/OIDC 登录授权端点的基础 URI
     * <p><b>协议归属：</b> OAuth2通用 + OIDC通用</p>
     * <p>用于发起认证请求的端点路径，用户访问此路径后会重定向到 IDP 进行登录。</p>
     * <p>默认值：{@code /oauth2/authorize}</p>
     * <p>访问示例：{@code /oauth2/authorize/{registrationId}}</p>
     */
    private String ssoAuthorizationUri = "/oauth2/authorize";

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
         * 在认证 URL 中会使用此 ID，例如：{@code /oauth2/authorize/{registrationId}}。</p>
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
         * OIDC 自动发现：IDP 的 Issuer URI
         * Oauth2也可以提供服务的自发现地址
         * 不管和Oauth2或Oidc，只需要.well-known前面的
         * <p>设置此值后，系统会自动从 {@code {oidcDiscoveryUri}/.well-known/openid-configuration}
         *      或 {@code {oauth2DiscoveryUri}/.well-known/oauth-authorization-server}
         * 发现授权端点、令牌端点、用户信息端点、JWKS URI 等所有元数据。
         * </p>
         * <p><b>与手动配置的关系：</b></p>
         * <ul>
         *   <li>配置此值后，{@code authorizationUri}、{@code tokenUri}、{@code userInfoUri} 等手动端点配置<b>不再需要</b></li>
         *   <li>不配置此值时，将使用手动指定的端点（传统 OAuth2 模式）</li>
         *   <li>自定义的配置会覆盖发现端点获取的配置</li>
         * </ul>
         * <p>示例：{@code https://your-idp.example.com}</p>
         * <p>参考：<a href="https://openid.net/specs/openid-connect-discovery-1_0.html">OpenID Connect Discovery</a></p>
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
         * <p><b>协议归属：</b> <b>OAuth2 手动配置专用</b>（OIDC 模式下默认使用 {@code sub}）</p>
         * <p>用户信息响应中表示用户唯一标识的属性名。</p>
         * <ul>
         *   <li><b>OAuth2 场景</b>：根据 IDP 返回的用户信息字段指定，如 {@code login}（GitHub）、{@code id}、{@code email} 等</li>
         *   <li><b>OIDC 场景</b>：使用 {@code oidcDiscoveryUri} 自动发现时，固定使用 {@code sub}（OIDC 标准主体标识）</li>
         * </ul>
         * <p>当 <b>未配置</b> {@code oidcDiscoveryUri} 时，此项为 <b>必填</b>。</p>
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
         * jwks的端点后缀参数，因为标准的jwks不带参数，但是有的IDP是每个sp都有不同的jwks，因此需要拼接相关的参数
         * 如果是你自己填了jwkSetUri，那么不建议使用此参数，直接jwkSetUri拼接好即可
         * eg:    ?client=1
         * eg:   /client1
         * @deprecated 暂时不这么用，麻烦又没多大意义
         */
        /*private String jwkSetParams;*/

        /**
         * 额外配置元数据
         * <p><b>协议归属：</b> <b>OIDC 专用</b>（由 {@code oidcDiscoveryUri} 自动发现时生成）</p>
         * <p>存储从 OIDC Discovery 文档中获取的其他元数据，如：</p>
         * <ul>
         *   <li>{@code end_session_endpoint} - OIDC RP-Initiated Logout 端点</li>
         *   <li>{@code revocation_endpoint} - 令牌撤销端点</li>
         *   <li>{@code claims_supported} - 支持的声明列表</li>
         *   <li>{@code response_types_supported} - 支持的响应类型</li>
         * </ul>
         * <p>通常由系统自动填充，无需手动配置。</p>
         */
        private Map<String, Object> configurationMetadata = Collections.emptyMap();

        /**
         * 客户端显示名称
         * <p><b>协议归属：</b> OAuth2通用 + OIDC通用</p>
         * <p>用于日志输出和调试的可读名称，非必填。</p>
         */
        private String clientName;

        /**
         * 是否忽略 SSL 证书校验
         * <p><b>协议归属：</b> OAuth2通用 + OIDC通用</p>
         * <p>设置为 {@code true} 时会跳过对 IDP 的 SSL 证书验证（<b>仅用于测试/开发环境</b>）。</p>
         * <p><b>⚠️ 生产环境务必保持默认值 {@code false}</b></p>
         */
        private boolean ignoreSsl = false;
    }
}
