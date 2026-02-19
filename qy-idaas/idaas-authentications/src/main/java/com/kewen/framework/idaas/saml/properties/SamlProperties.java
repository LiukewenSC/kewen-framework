package com.kewen.framework.idaas.saml.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.List;

/**
 * SAML2 认证属性配置
 * <p>
 * 支持两种配置方式：
 * <ul>
 *   <li>方式一（推荐）：配置 IdP 的 metadata.xml 文件路径，自动解析 entityId、webSsoUrl 和证书</li>
 *   <li>方式二：手动配置 entityId、webSsoUrl 和证书路径</li>
 * </ul>
 *
 * @author kewen
 * @since 1.0
 */
@ConfigurationProperties("kewen.security.login.saml")
@Data
public class SamlProperties {

    /**
     * SP 发起 SAML 认证请求的 URL，默认为 {baseUrl}/login/saml2/sso/{registrationId}
     * template (i.e. "{baseUrl}/login/saml2/sso/{registrationId
     * @see RelyingPartyRegistration.Builder#assertionConsumerServiceUrlTemplate(String)
     */
    private String loginProcessingUrl = "/login/saml2/sso/{registrationId}";

    private List<RegistrationSamlProperties> registrations;


    /**
     * 自定义的登录入口地址，用户访问此地址将触发 SAML 认证流程
     * 暂时还没有实现，目前SpringSecurity5.3没有地方可以修改，如果确实需要的话只能重定向至对应的地址
     * 默认为 null，表示使用 Spring Security 默认的 /saml2/authenticate/{registrationId}
     * 配置后，需要创建对应的控制器来重定向到 SAML 认证地址
     * 例如配置为 /sso/login，用户访问 /sso/login 时会自动跳转到 IdP 进行认证
     */
    @Deprecated
    private String loginPageUrl;

    @Data
    public static class RegistrationSamlProperties{


        /**
         * IdP metadata.xml 文件路径，配置后将自动解析 entityId、webSsoUrl 和证书，
         * 优先级高于手动配置的 entityId、webSsoUrl、idpCertificateResource
         */
        private Resource metadataResource = new ClassPathResource("saml/idp-metadata.xml");
        /**
         * SP 注册 ID，用于标识当前 SAML 配置
         * SpringSecurity 专属配置，跟SAML协议无关
         * = "kewen-saml-registration"
         */
        private String registrationId ;

        /**
         * SP Entity ID，即本应用作为 Service Provider 的标识。
         * 需要与 IdP 侧配置的 SP entityId（Audience）保持一致，
         * 否则 SAML Response 中的 AudienceRestriction 校验会失败。
         * 默认使用 registrationId 的值
         */
        private String spEntityId = "kewen-saml";

        public String getRegistrationId() {
            if (registrationId == null) {
                return spEntityId;
            }
            return registrationId;
        }
    }


/*    *//**
     * 是否使用 metadata.xml 自动解析，默认 true。
     * 设为 false 时使用手动配置的 entityId、webSsoUrl、idpCertificateResource
     *//*
    private boolean useMetadata = true;

    *//**
     * IdP Entity ID，useMetadata=false 时生效
     *//*
    private String idpIdentityId = "idaas-idp4";

    *//**
     * IdP SSO 登录地址，useMetadata=false 时生效
     *//*
    private String webSsoUrl = "https://idp4-test.alibaba.net/sso/saml";

    *//**
     * IdP 证书资源路径，useMetadata=false 时生效，默认 classpath:saml/idp-certificate.crt
     *//*
    private Resource idpCertificateResource = new ClassPathResource("saml/idp-certificate.crt");*/
}