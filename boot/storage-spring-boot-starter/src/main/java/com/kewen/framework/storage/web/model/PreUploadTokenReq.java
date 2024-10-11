package com.kewen.framework.storage.web.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 预上传文件名
 * @author kewen
 * @since 2023-06-21
 */
@Data
public class PreUploadTokenReq {
    /**
     * 文件名
     */
    @NotBlank(message = "文件名不能为空")
    String fileName;
}
