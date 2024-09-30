package com.kewen.framework.storage.web.model;

import lombok.Data;

/**
 * 预上传文件名
 * @author kewen
 * @since 2023-06-21
 */
@Data
public class PreUploadTokenReq {
    /**
     * 文件名，可支持 /目录
     */
    String fileName;
}
