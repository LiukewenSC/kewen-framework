package com.kewen.framework.auth.security.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 方糖的参数
 * @author kewen
 * @since 2024-011-26
 */
@ConfigurationProperties("kewen.security.session")
@Data
public class SessionProperties {
    /**
     * 最大session数量
     */
    private int maximumSessions = 10;
    /**
     * 到了最大session是阻止还是过期最久的(最早的踢下线)
     */
    private boolean maxSessionsPreventsLogin = false;
}
