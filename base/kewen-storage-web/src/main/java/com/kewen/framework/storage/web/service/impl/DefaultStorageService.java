package com.kewen.framework.storage.web.service.impl;

import com.kewen.framework.common.core.exception.BizException;
import com.kewen.framework.storage.core.StorageTemplate;
import com.kewen.framework.storage.core.model.FileInfo;
import com.kewen.framework.storage.core.model.PreUploadTokenBO;
import com.kewen.framework.storage.core.model.UploadBO;
import com.kewen.framework.storage.web.model.UploadCallbackReq;
import com.kewen.framework.storage.web.mp.entity.SysStorageFile;
import com.kewen.framework.storage.web.mp.entity.SysStorageModule;
import com.kewen.framework.storage.web.mp.service.SysStorageFileMpService;
import com.kewen.framework.storage.web.mp.service.SysStorageModuleMpService;
import com.kewen.framework.storage.web.service.StorageService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author kewen
 * @since 2023-06-20
 */
public class DefaultStorageService implements StorageService {


    @Autowired
    StorageTemplate storageTemplate;

    private String downloadDomain;

    @Autowired
    SysStorageFileMpService storageFileMpService;

    @Autowired
    SysStorageModuleMpService storageModuleMpService;
    
    @Override
    public PreUploadTokenBO genUploadToken(String moduleName, String fileName) {
        String relativeDirectory = getRelativeDirectory(moduleName);

        SysStorageFile file = new SysStorageFile()
                .setFileName(fileName)
                .setRelativeDirectory(relativeDirectory)
                .setStatus(0);
        storageFileMpService.save(file);

        PreUploadTokenBO preUploadToken = storageTemplate.createPreUploadToken(relativeDirectory, fileName);

        preUploadToken.setFileId(file.getId());


        return preUploadToken;
    }

    @Override
    public void uploadCallback(UploadCallbackReq req) {

        SysStorageFile storageFile = storageFileMpService.getById(req.getFileId());

        storageFile.setMimeType(req.getMimeType())
                .setSize(req.getSize())
                .setStatus(2);
        storageFileMpService.updateById(storageFile);

    }

    /**
     * @param fileId 文件id
     * @param update 0-等待上传 1-上传中 2-完成 3-失败
     */
    void  updateFileStatus(Long fileId, Integer update){
        SysStorageFile storageFile = storageFileMpService.getById(fileId);
        storageFile.setStatus(update);
        storageFileMpService.updateById(storageFile);

    }


    @Override
    public FileInfo upload(String moduleName,String fileName, String mimeType, InputStream inputStream) {

        //上传
        UploadBO upload = storageTemplate.upload(inputStream, fileName, mimeType);
        String relativeDirectory =getRelativeDirectory(moduleName);


        SysStorageFile file = new SysStorageFile()
                .setRelativeDirectory(relativeDirectory)
                .setFileName(fileName)
                .setMimeType(mimeType)
                .setSize(upload.getSize())
                ;
        storageFileMpService.save(file);
        //存储
       return new FileInfo(file.getId(),fileName,fullUrl(relativeDirectory,fileName),upload.getSize());
    }
    private String getRelativeDirectory(String moduleName){
        SysStorageModule module = storageModuleMpService.getById(moduleName);
        if (module==null){
            throw new BizException("未查询到模块，请配置");
        }
        return module.getRelativeDirectory();
    }


    @Override
    public FileInfo getDownloadInfo(Long fileId) {
        SysStorageFile byId = storageFileMpService.getById(fileId);
        if (byId ==null){
            return null;
        }
        return new FileInfo(byId.getId(), byId.getFileName(), fullUrl(byId.getRelativeDirectory(),byId.getFileName()), byId.getSize());
    }

    @Override
    public List<FileInfo> listDownloadInfo(List<Long> fileIds) {
        List<SysStorageFile> SysStorageFiles = storageFileMpService.listByIds(fileIds);
        if (CollectionUtils.isEmpty(SysStorageFiles)) {
            return Collections.emptyList();
        }
        return SysStorageFiles.stream()
                .map(f -> new FileInfo(f.getId(), f.getFileName(), fullUrl(f.getRelativeDirectory(),f.getFileName()), f.getSize()))
                .collect(Collectors.toList());
    }

    private String fullUrl(String relativeDirectory,String fileName) {
        return "http://" + downloadDomain + "/" +  relativeDirectory + "/" + fileName;
    }

    public void setDownloadDomain(String downloadDomain) {
        this.downloadDomain = downloadDomain;
    }

    public void setStorageTemplate(StorageTemplate storageTemplate) {
        this.storageTemplate = storageTemplate;
    }

    public void setStorageFileMpService(SysStorageFileMpService storageFileMpService) {
        this.storageFileMpService = storageFileMpService;
    }

    public void setStorageModuleMpService(SysStorageModuleMpService storageModuleMpService) {
        this.storageModuleMpService = storageModuleMpService;
    }
}
