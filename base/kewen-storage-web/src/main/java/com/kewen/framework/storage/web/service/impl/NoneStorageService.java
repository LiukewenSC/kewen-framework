package com.kewen.framework.storage.web.service.impl;

import com.kewen.framework.storage.web.model.UploadResult;
import com.kewen.framework.storage.web.service.StorageService;

import java.util.List;

/**
 * 
 * @author kewen
 * @since 2023-04-24
 */
public class NoneStorageService implements StorageService {


    @Override
    public UploadResult save(String name, String suffix, String storageName, String path, String mimeType, Long size) {

        return null;
    }

    @Override
    public UploadResult getDownloadInfo(Long fileId) {
        return null;
    }

    @Override
    public List<UploadResult> listDownloadInfo(List<Long> fileIds) {
        return null;
    }
}
