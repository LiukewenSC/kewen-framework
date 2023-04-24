package com.kewen.framework.storage.web.service.impl;

import com.kewen.framework.storage.web.model.UploadResult;
import com.kewen.framework.storage.web.mp.entity.SysFileStorage;
import com.kewen.framework.storage.web.mp.service.SysFileStorageMpService;
import com.kewen.framework.storage.web.service.StorageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author kewen
 * @since 2023-04-24
 */
public class SysFileStorageService implements StorageService {

    @Autowired
    SysFileStorageMpService fileStorageMpService;

    @Override
    public void save(String name ,String suffix,String storageName, String path,String mimeType,Long size) {
        fileStorageMpService.save(
                new SysFileStorage()
                        .setFileName(name)
                        .setStorageName(storageName)
                        .setPath(StringUtils.isBlank(path)?storageName:path)
                        .setFileType(suffix)
                        .setMimeType(mimeType)
                        .setSize(size)
        );
    }

    @Override
    public UploadResult getDownloadUrl(Long fileId) {
        SysFileStorage byId = fileStorageMpService.getById(fileId);
        new UploadResult(byId.getFileName(), byId.getPath(), byId.getSize());
        return null;
    }
}
