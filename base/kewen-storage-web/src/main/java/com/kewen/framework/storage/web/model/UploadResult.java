package com.kewen.framework.storage.web.model;

import lombok.Data;
import lombok.Getter;

/**
 * 
 * @author kewen
 * @since 2023-04-24
 */
@Data
public class UploadResult {

    private Long fileId;
    private String fileName;
    private String url;
    private Long size;

    public UploadResult(Long fileId,String fileName, String url, Long size) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.url = url;
        this.size = size;
    }
}
