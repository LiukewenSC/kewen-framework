package com.cu.framework.storage.boot;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 *
 * 存储配置
 * @author kewen
 * @since 2023-04-24
 */
@Data
@ConfigurationProperties("cu.storage")
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
    /**
     * 存储桶是否为公开空间；为 false 时生成的下载链接会带有效期签名
     */
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
