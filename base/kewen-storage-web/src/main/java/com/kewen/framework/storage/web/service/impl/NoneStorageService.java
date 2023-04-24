package com.kewen.framework.storage.web.service.impl;

import com.kewen.framework.storage.web.model.UploadResult;
import com.kewen.framework.storage.web.service.StorageService;

/**
 * 
 * @author kewen
 * @since 2023-04-24
 */
public class NoneStorageService implements StorageService {


    @Override
    public void save(String name, String suffix, String storageName, String path, String mimeType, Long size) {

    }

    @Override
    public UploadResult getDownloadUrl(Long fileId) {
        return null;
    }
}
