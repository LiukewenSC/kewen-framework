package com.kewen.framework.boot.basic.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 方糖的参数
 * @author kewen
 * @since 2024-08-30
 */
@ConfigurationProperties("kewen.message.fang-tang")
public class FangTangProperties {
    /**
     * 秘钥
     */
    private String key = "SCT248328TA-iYbvN5Vab4ixXBUOqA60fLY5";
    /**
     * 链接
     */
    private String domain ="https://sctapi.ftqq.com";


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
