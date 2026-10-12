package com.cu.framework.storage.web.model;

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
public class PreUploadTokenResp {

    /** 预保存的文件记录 ID（sys_storage_file 表主键） */
    private Long fileId;
    /** 文件在存储中的完整 key */
    private String key;
    /** 上传凭证，客户端凭此直传存储 */
    private String uploadToken;
}
