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
     * 当前用户接口地址；由 auth-rbac-security-spring-boot-starter 的 /currentUser 端点读取。
     * 仅引入 login-spring-boot-starter 时不会提供该端点，本项不生效
     */
    private String currentUserUrl = "/currentUser";


}
