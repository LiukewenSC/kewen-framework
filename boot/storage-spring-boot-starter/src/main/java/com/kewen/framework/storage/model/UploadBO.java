package com.kewen.framework.storage.model;

import lombok.Data;

/**
 *  文件上传返回结果
 * @author kewen
 * @since 2023-04-24
 */
@Data
public class UploadBO {
    private String key;
    private String hash;

    private Long size;
}
