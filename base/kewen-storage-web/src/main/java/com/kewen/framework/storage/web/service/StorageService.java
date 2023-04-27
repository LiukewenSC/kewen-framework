package com.kewen.framework.storage.web.service;

import com.kewen.framework.storage.web.model.FileInfo;

import java.util.List;

/**
 * 
 * @author kewen
 * @since 2023-04-24
 */
public interface StorageService {

    /**
     * 保存
     * @param name 文件名
     * @param suffix 后缀名
     * @param storageName 对象存储名
     * @param path 对象存储访问链接
     * @param mimeType 文件类型
     * @param size 大小
     */
    FileInfo save(String name , String suffix, String storageName, String path, String mimeType, Long size);

    /**
     * 获取下载链接
     * @param fileId
     * @return
     */
    FileInfo getDownloadInfo(Long fileId);

    /**
     * 获取下载信息
     * @param fileIds
     * @return
     */
    List<FileInfo> listDownloadInfo(List<Long> fileIds);
}
