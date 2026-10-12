package com.cu.framework.storage.core.model;

import lombok.Data;

/**
 * 
 * @author kewen
 * @since 2023-04-24
 */
@Data
public class FileInfo {

    /** 文件 ID */
    private Long fileId;
    /** 文件名 */
    private String fileName;
    /** 文件访问链接 */
    private String url;
    /** 文件大小（字节） */
    private Long size;

    public FileInfo(Long fileId, String fileName, String url, Long size) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.url = url;
        this.size = size;
    }
}
