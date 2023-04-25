package com.kewen.framework.storage.web.service.impl;

import com.kewen.framework.storage.web.model.UploadResult;
import com.kewen.framework.storage.web.mp.entity.SysFileStorage;
import com.kewen.framework.storage.web.mp.service.SysFileStorageMpService;
import com.kewen.framework.storage.web.service.StorageService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author kewen
 * @since 2023-04-24
 */
public class SysFileStorageService implements StorageService {

    @Autowired
    SysFileStorageMpService fileStorageMpService;

    private String downloadDomain = "http://localhost:8080";

    @Override
    public UploadResult save(String name, String suffix, String storageName, String path, String mimeType, Long size) {
        SysFileStorage storage = new SysFileStorage()
                .setFileName(name)
                .setStorageName(storageName)
                .setPath(StringUtils.isBlank(path) ? storageName : path)
                .setFileType(suffix)
                .setMimeType(mimeType)
                .setSize(size);
        boolean save = fileStorageMpService.save(storage);
        return new UploadResult(storage.getId(), name, fullUrl(path), size);
    }

    @Override
    public UploadResult getDownloadInfo(Long fileId) {
        SysFileStorage byId = fileStorageMpService.getById(fileId);
        if (byId ==null){
            return null;
        }
        return new UploadResult(byId.getId(), byId.getFileName(), fullUrl(byId.getPath()), byId.getSize());
    }

    @Override
    public List<UploadResult> listDownloadInfo(List<Long> fileIds) {
        List<SysFileStorage> sysFileStorages = fileStorageMpService.listByIds(fileIds);
        if (CollectionUtils.isEmpty(sysFileStorages)) {
            return Collections.emptyList();
        }
        return sysFileStorages.stream()
                .map(f -> new UploadResult(f.getId(), f.getFileName(), fullUrl(f.getPath()), f.getSize()))
                .collect(Collectors.toList());
    }

    private String fullUrl(String path) {
        return "http://" + downloadDomain + "/" + path;
    }

    public void setDownloadDomain(String downloadDomain) {
        this.downloadDomain = downloadDomain;
    }
}
