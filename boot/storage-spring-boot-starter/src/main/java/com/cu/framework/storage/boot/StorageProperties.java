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
    /**
     * 存储区域，取值为七牛的区域标识：region0/huadong、region1/huabei、region2/huanan、
     * region3/beimei、region4/xinjiapo、auto。
     * 默认 region2，与改造前在 StorageAutoConfiguration 中硬编码的机房保持一致
     */
    private String region = "region2";
    /**
     * 数据处理（转码等）的异步通知地址；为空时上传凭证中不写入 persistentNotifyUrl 策略。
     * 不提供默认值——该地址属于具体部署环境，框架内置会让所有使用方的回调打到同一个域名
     */
    private String persistentNotifyUrl;
    /**
     * 生成的下载链接是否使用 https。
     * 依赖 downloadDomain 已在存储侧配置好证书，因此默认 false 以保持现有部署的行为不变
     */
    private Boolean useHttpsDownload = false;
}
