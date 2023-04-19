package com.kewen.framework.boot.auth;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

/**
 * @author kewen
 * @since 2023-04-14
 */
@ConfigurationProperties("kewen.auth")
@Data
public class AuthProperties {
    private String loginEndpoint= "/login";
    private String logoutEndpoint= "/logout";
    private String permitUrl;

    private Store store = new Store();

    public String[] getPermitUrls(){
        if (org.apache.commons.lang3.StringUtils.isBlank(permitUrl)){
            return new String[0];
        }
        return StringUtils.tokenizeToStringArray( permitUrl,",;");
    }

    @Data
    public static class Store {
        private AuthStoreType type = AuthStoreType.SESSION;;
        private Long expireTime = 7200L;

    }
}
