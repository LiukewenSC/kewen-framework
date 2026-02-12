package com.kewen.framework.auth.security.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 2026/02/12
 *
 * @author kewen
 * @since 1.0
 */

@ConfigurationProperties("kewen.security")
@Data
public class SecurityProperties {
    /**
     * 当前用户登录
     */
    private String currentUserUrl = "/currentUser";


}
