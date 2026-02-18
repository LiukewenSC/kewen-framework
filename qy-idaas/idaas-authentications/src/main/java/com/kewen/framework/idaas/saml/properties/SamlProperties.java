package com.kewen.framework.idaas.saml.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

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
     * SP 注册 ID，用于标识当前 SAML 配置
     */
    private String registrationId = "kewen-saml";

    /**
     * SP Entity ID，即本应用作为 Service Provider 的标识。
     * 需要与 IdP 侧配置的 SP entityId（Audience）保持一致，
     * 否则 SAML Response 中的 AudienceRestriction 校验会失败。
     * 默认使用 registrationId 的值
     */
    private String spEntityId = "kewen-saml";

    /**
     * IdP metadata.xml 文件路径，配置后将自动解析 entityId、webSsoUrl 和证书，
     * 优先级高于手动配置的 entityId、webSsoUrl、idpCertificateResource
     */
    private Resource metadataResource = new ClassPathResource("saml/idp-metadata.xml");

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
