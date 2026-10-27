package com.cu.framework.boot.basic.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 方糖的参数
 * @author kewen
 * @since 2024-08-30
 */
@ConfigurationProperties("cu.notify.fang-tang")
public class FangTangProperties {

    /**
     * 是否启用方糖消息客户端；为 true 时 key 必填
     */
    private boolean enabled;
    /**
     * 秘钥
     */
    private String key;
    /**
     * 链接
     */
    private String domain ="https://sctapi.ftqq.com";


    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }
}
