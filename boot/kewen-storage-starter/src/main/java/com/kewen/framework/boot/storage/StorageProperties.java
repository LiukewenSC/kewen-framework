package com.kewen.framework.boot.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 *
 * 存储配置
 * @author kewen
 * @since 2023-04-24
 */
@Data
@ConfigurationProperties("kewen.storage")
public class StorageProperties {
    private String type;
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String downloadDomain;
    private String uploadCallbackUrl;
}
