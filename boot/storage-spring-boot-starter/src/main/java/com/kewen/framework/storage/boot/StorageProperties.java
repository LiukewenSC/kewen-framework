package com.kewen.framework.storage.boot;

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
    /**
     * 类型 qiniu
     */
    private String type;
    /**
     * accessKey
     */
    private String accessKey;
    /**
     * secretKey
     */
    private String secretKey;
    /**
     * 存储捅空间
     */
    private String bucket;
    /**
     * 存储根目录
     */
    private String rootPath ="";
    private Boolean isPublic = true;
    /**
     * 下载域，即下载图片时访问的域名地址
     */
    private String downloadDomain;
    /**
     * 回调地址，上传成功后的回调地址
     */
    private String uploadCallbackUrl;
}
