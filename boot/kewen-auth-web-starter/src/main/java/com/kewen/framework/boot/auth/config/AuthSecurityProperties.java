package com.kewen.framework.boot.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

/**
 * @author kewen
 * @since 2023-04-14
 */
@ConfigurationProperties("kewen.auth")
@Data
public class AuthSecurityProperties {
    private String loginEndpoint;
    private String logoutEndpoint;
    private String permitUrl;

    public String[] getPermitUrls(){
        return StringUtils.tokenizeToStringArray( permitUrl,",;");
    }

}
