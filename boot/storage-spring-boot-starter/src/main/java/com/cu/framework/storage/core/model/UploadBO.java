package com.cu.framework.storage.core.model;

import lombok.Data;

/**
 *  文件上传返回结果
 * @author kewen
 * @since 2023-04-24
 */
@Data
public class UploadBO {
    /** 上传后文件在存储中的 key */
    private String key;
    /** 文件哈希（七牛 etag） */
    private String hash;

    /** 文件大小（字节） */
    private Long size;
}
