package com.cu.framework.authentication.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 2026/02/12
 *
 * @author kewen
 * @since 1.0
 */

@ConfigurationProperties("cu.security")
@Data
public class SecurityProperties {
    /**
     * 当前用户接口地址；当前代码未读取，配置后不生效，接口路径固定为 /currentUser
     */
    private String currentUserUrl = "/currentUser";


}
