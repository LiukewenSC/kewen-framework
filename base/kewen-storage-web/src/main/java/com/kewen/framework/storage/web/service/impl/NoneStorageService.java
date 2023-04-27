package com.kewen.framework.storage.web.service.impl;

import com.kewen.framework.storage.web.model.FileInfo;
import com.kewen.framework.storage.web.service.StorageService;

import java.util.List;

/**
 * 
 * @author kewen
 * @since 2023-04-24
 */
public class NoneStorageService implements StorageService {


    @Override
    public FileInfo save(String name, String suffix, String storageName, String path, String mimeType, Long size) {

        return null;
    }

    @Override
    public FileInfo getDownloadInfo(Long fileId) {
        return null;
    }

    @Override
    public List<FileInfo> listDownloadInfo(List<Long> fileIds) {
        return null;
    }
}
