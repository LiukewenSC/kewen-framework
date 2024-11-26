package com.kewen.framework.auth.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 方糖的参数
 * @author kewen
 * @since 2024-011-26
 */
@ConfigurationProperties("kewen.security.remember-me")
@Data
public class RememberMeProperties {
    /**
     * 是否开启记住我
     */
    private boolean enabled = true;
    /**
     * 记住我的key
     */
    private String rememberParameter = "remember-me";
    /**
     * 记住时间，单位秒，默认30天
     */
    private Integer validitySeconds = 2592000;
}
