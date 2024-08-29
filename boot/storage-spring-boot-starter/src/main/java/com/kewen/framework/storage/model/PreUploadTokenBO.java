package com.kewen.framework.storage.model;

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
    private String key;
    private String uploadToken;
}
