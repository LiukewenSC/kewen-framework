package com.kewen.framework.storage.model;

import lombok.Data;

/**
 * 
 * @author kewen
 * @since 2023-04-24
 */
@Data
public class FileInfo {

    private Long fileId;
    private String fileName;
    private String url;
    private Long size;

    public FileInfo(Long fileId, String fileName, String url, Long size) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.url = url;
        this.size = size;
    }
}
