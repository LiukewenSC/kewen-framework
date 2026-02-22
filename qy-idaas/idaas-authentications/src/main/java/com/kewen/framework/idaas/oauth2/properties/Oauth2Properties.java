package com.kewen.framework.idaas.oauth2.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.oauth2.core.AuthenticationMethod;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import java.util.*;

/**
 * 2026/02/19
 *
 * @author kewen
 * @since 1.0
 */
@ConfigurationProperties("kewen.security.login.oauth2")
@Data
public class Oauth2Properties {
    private String authorizationUri = "/oauth2/authorize";
    private String loginProcessingUrl = OAuth2LoginAuthenticationFilter.DEFAULT_FILTER_PROCESSES_URI;

    private List<RegistrationClientProperties> clients ;

    @Data
    public static class RegistrationClientProperties {
        private String registrationId;
        private String clientId;
        private String clientSecret;
        private ClientAuthenticationMethod clientAuthenticationMethod = ClientAuthenticationMethod.BASIC;
        private AuthorizationGrantType authorizationGrantType;
        private String redirectUriTemplate = "{baseUrl}/login/oauth2/code/{registrationId}";
        private Set<String> scopes;
        private String authorizationUri;
        private String tokenUri;
        private String userInfoUri;
        private AuthenticationMethod userInfoAuthenticationMethod = AuthenticationMethod.HEADER;
        private String userNameAttributeName;
        private String jwkSetUri;
        private Map<String, Object> configurationMetadata = Collections.emptyMap();
        private String clientName;
        private boolean ignoreSsl = false;
    }
}
