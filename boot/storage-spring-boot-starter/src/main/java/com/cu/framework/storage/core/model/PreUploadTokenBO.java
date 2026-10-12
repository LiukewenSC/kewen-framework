package com.cu.framework.storage.core.model;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 预上传凭证
 *
 * @author kewen
 * @since 2023-06-19
 */
@Data
@Accessors(chain = true)
public class PreUploadTokenBO {
    /** 文件在存储中的完整 key（根目录/相对目录/文件名） */
    private String key;
    /** 上传凭证，客户端凭此直传存储 */
    private String uploadToken;
}
