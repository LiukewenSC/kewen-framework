package com.cu.framework.storage.web.model;

import lombok.Data;

/**
 * @author kewen
 * @since 2023-06-19
 */
@Data
public class UploadCallbackReq {
    /** 文件记录 ID，对应回调 body 中的 x:fileId 自定义变量 */
    private Long fileId;
    /** 文件在存储中的 key */
    private String key;
    /** 文件哈希（七牛 etag） */
    private String hash;
    /** 存储桶名称 */
    private String bucket;
    /** 文件大小（字节，七牛 fsize） */
    private Long size;
    /** 文件 MIME 类型 */
    private String mimeType;


}
