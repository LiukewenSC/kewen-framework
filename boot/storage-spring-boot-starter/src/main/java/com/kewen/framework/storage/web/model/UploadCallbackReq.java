package com.kewen.framework.storage.web.model;

import lombok.Data;

/**
 * @author kewen
 * @since 2023-06-19
 */
@Data
public class UploadCallbackReq {
    private Long fileId;
    private String key;
    private String hash;
    private String bucket;
    private Long size;
    private String mimeType;


}
