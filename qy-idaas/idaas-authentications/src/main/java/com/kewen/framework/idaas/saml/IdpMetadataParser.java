package com.kewen.framework.idaas.saml;

import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import org.apache.commons.lang3.tuple.Pair;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.UnmarshallerFactory;
import org.opensaml.saml.saml2.metadata.*;
import org.opensaml.security.credential.UsageType;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.X509Data;
import org.springframework.core.io.Resource;
import org.springframework.security.saml2.provider.service.registration.Saml2MessageBinding;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.List;

/**
 * IdP metadata.xml 解析器
 * <p>
 * 负责从 IdP 提供的 metadata.xml 文件中解析出 Entity ID、SSO URL 和签名证书等信息。
 * 解析结果封装在 {@link IdpMetadata} 中，供 SAML 配置使用。
 * <p>
 * 此类独立于 Spring Security 版本，方便后续升级时替换上层配置而不影响解析逻辑。
 *
 * @author kewen
 * @since 1.0
 */
public class IdpMetadataParser {

    private static final String SAML2_PROTOCOL = "urn:oasis:names:tc:SAML:2.0:protocol";
    private static final String HTTP_POST_BINDING = "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST";
    private static final String HTTP_REDIRECT_BINDING = "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect";

    /**
     * 从 metadata.xml 资源文件中解析 IdP 元数据信息
     *
     * @param metadataResource metadata.xml 资源
     * @return 解析后的 IdP 元数据
     */
    public IdpMetadata parse(Resource metadataResource) {
        try {
            InitializationService.initialize();

            EntityDescriptor entityDescriptor = parseEntityDescriptor(metadataResource);
            String entityId = entityDescriptor.getEntityID();

            IDPSSODescriptor idpDescriptor = entityDescriptor.getIDPSSODescriptor(SAML2_PROTOCOL);
            if (idpDescriptor == null) {
                throw new IllegalStateException("metadata.xml 中未找到 IDPSSODescriptor");
            }
            Pair<Saml2MessageBinding,String> ssoPair = extractSsoUrl(idpDescriptor);
            Pair<Saml2MessageBinding,String> logoutPair = extractLogoutUrl(idpDescriptor);
            X509Certificate certificate = extractCertificate(idpDescriptor);

            Boolean wantAuthnRequestsSigned = idpDescriptor.getWantAuthnRequestsSigned();
            return new IdpMetadata(entityId,  ssoPair.getRight(),ssoPair.getLeft(), certificate,wantAuthnRequestsSigned, logoutPair.getRight(), logoutPair.getLeft());
        } catch (Exception e) {
            throw new IllegalStateException("解析 IdP metadata.xml 失败: " + metadataResource, e);
        }
    }

    /**
     * 解析 metadata.xml 为 EntityDescriptor
     */
    private EntityDescriptor parseEntityDescriptor(Resource metadataResource) throws Exception {
        BasicParserPool parserPool = new BasicParserPool();
        parserPool.initialize();

        try (InputStream inputStream = metadataResource.getInputStream()) {
            Document document = parserPool.parse(inputStream);
            Element element = document.getDocumentElement();

            UnmarshallerFactory unmarshallerFactory = XMLObjectProviderRegistrySupport.getUnmarshallerFactory();
            XMLObject xmlObject = unmarshallerFactory.getUnmarshaller(element).unmarshall(element);

            if (xmlObject instanceof EntityDescriptor) {
                return (EntityDescriptor) xmlObject;
            }
            throw new IllegalStateException("metadata.xml 根元素不是 EntityDescriptor");
        }
    }

    /**
     * 从 IDPSSODescriptor 中提取 SSO URL，优先使用 HTTP-POST 绑定，其次 HTTP-Redirect
     */
    private Pair<Saml2MessageBinding,String> extractSsoUrl(IDPSSODescriptor idpDescriptor) {
        List<SingleSignOnService> ssoServices = idpDescriptor.getSingleSignOnServices();

        for (SingleSignOnService ssoService : ssoServices) {
            if (HTTP_POST_BINDING.equals(ssoService.getBinding())) {
                return Pair.of(Saml2MessageBinding.POST, ssoService.getLocation());
            }
        }
        for (SingleSignOnService ssoService : ssoServices) {
            if (HTTP_REDIRECT_BINDING.equals(ssoService.getBinding())) {
                return Pair.of(Saml2MessageBinding.REDIRECT, ssoService.getLocation());
            }
        }
        /*if (!ssoServices.isEmpty()) {
            return ssoServices.get(0).getLocation();
        }*/
        throw new IllegalStateException("metadata.xml 中未找到 SingleSignOnService");
    }
    private Pair<Saml2MessageBinding,String> extractLogoutUrl(IDPSSODescriptor idpDescriptor) {
        List<SingleLogoutService> ssoServices = idpDescriptor.getSingleLogoutServices();

        for (SingleLogoutService ssoService : ssoServices) {
            if (HTTP_POST_BINDING.equals(ssoService.getBinding())) {
                return Pair.of(Saml2MessageBinding.POST, ssoService.getLocation());
            }
        }
        for (SingleLogoutService ssoService : ssoServices) {
            if (HTTP_REDIRECT_BINDING.equals(ssoService.getBinding())) {
                return Pair.of(Saml2MessageBinding.REDIRECT, ssoService.getLocation());
            }
        }
        /*if (!ssoServices.isEmpty()) {
            return ssoServices.get(0).getLocation();
        }*/
        throw new IllegalStateException("metadata.xml 中未找到 SingleSignOnService");
    }
    /**
     * 从 IDPSSODescriptor 中提取用于签名验证的 X509 证书
     */
    private X509Certificate extractCertificate(IDPSSODescriptor idpDescriptor) {
        List<KeyDescriptor> keyDescriptors = idpDescriptor.getKeyDescriptors();

        for (KeyDescriptor keyDescriptor : keyDescriptors) {
            if (keyDescriptor.getUse() == null || keyDescriptor.getUse() == UsageType.SIGNING) {
                KeyInfo keyInfo = keyDescriptor.getKeyInfo();
                if (keyInfo != null) {
                    List<X509Data> x509DataList = keyInfo.getX509Datas();
                    for (X509Data x509Data : x509DataList) {
                        List<org.opensaml.xmlsec.signature.X509Certificate> certificates = x509Data.getX509Certificates();
                        for (org.opensaml.xmlsec.signature.X509Certificate cert : certificates) {
                            return parseCertificate(cert.getValue());
                        }
                    }
                }
            }
        }
        throw new IllegalStateException("metadata.xml 中未找到签名证书");
    }

    /**
     * 将 Base64 编码的证书字符串解析为 X509Certificate
     */
    private X509Certificate parseCertificate(String base64Certificate) {
        try {
            String cleaned = base64Certificate.replaceAll("\\s+", "");
            byte[] decoded = Base64.getDecoder().decode(cleaned);
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            return (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(decoded));
        } catch (Exception e) {
            throw new IllegalStateException("解析证书失败", e);
        }
    }

    /**
     * IdP 元数据解析结果，包含 entityId、ssoUrl 和签名证书
     */
    public static class IdpMetadata {
        private final String entityId;
        private final String ssoUrl;
        private final Saml2MessageBinding ssoBinding;
        private final X509Certificate signingCertificate;
        private final boolean wantAuthnRequestsSigned;
        private final String logoutUrl;
        private final Saml2MessageBinding logoutBinding;

        public IdpMetadata(String entityId, String ssoUrl, Saml2MessageBinding ssoBinding, X509Certificate signingCertificate, boolean wantAuthnRequestsSigned, String logoutUrl, Saml2MessageBinding logoutBinding) {
            this.entityId = entityId;
            this.ssoUrl = ssoUrl;
            this.ssoBinding = ssoBinding;
            this.signingCertificate = signingCertificate;
            this.wantAuthnRequestsSigned = wantAuthnRequestsSigned;
            this.logoutUrl = logoutUrl;
            this.logoutBinding = logoutBinding;
        }

        public String getEntityId() {
            return entityId;
        }

        public String getSsoUrl() {
            return ssoUrl;
        }

        public X509Certificate getSigningCertificate() {
            return signingCertificate;
        }

        public String getLogoutUrl() {
            return logoutUrl;
        }

        public Saml2MessageBinding getLogoutBinding() {
            return logoutBinding;
        }

        public Saml2MessageBinding getSsoBinding() {
            return ssoBinding;
        }

        public boolean isWantAuthnRequestsSigned() {
            return wantAuthnRequestsSigned;
        }
    }
}
