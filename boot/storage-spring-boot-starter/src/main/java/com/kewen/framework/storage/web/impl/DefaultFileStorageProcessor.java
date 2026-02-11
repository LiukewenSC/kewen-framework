package com.kewen.framework.storage.web.impl;

import com.kewen.framework.storage.core.StorageTemplate;
import com.kewen.framework.storage.core.model.FileInfo;
import com.kewen.framework.storage.core.model.PreUploadTokenBO;
import com.kewen.framework.storage.core.model.UploadBO;
import com.kewen.framework.storage.web.FileStorageProcessor;
import com.kewen.framework.storage.web.model.PreUploadTokenResp;
import com.kewen.framework.storage.web.model.UploadCallbackReq;
import com.kewen.framework.storage.web.mp.entity.SysStorageFile;
import com.kewen.framework.storage.web.mp.service.SysStorageFileMpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author kewen
 * @since 2023-06-20
 */
public class DefaultFileStorageProcessor implements FileStorageProcessor {


    @Autowired
    StorageTemplate storageTemplate;

    @Autowired
    SysStorageFileMpService storageFileMpService;
    
    @Override
    public PreUploadTokenResp genUploadToken(String relativeDirectory, String fileName) {


        PreUploadTokenBO preUploadToken = storageTemplate.createPreUploadToken(relativeDirectory, fileName);

        SysStorageFile file = new SysStorageFile()
                .setFileName(fileName)
                .setFileKey(preUploadToken.getKey())
                .setStatus(0);
        storageFileMpService.save(file);

        PreUploadTokenResp resp = new PreUploadTokenResp();
        resp.setFileId(file.getId())
                .setKey(preUploadToken.getKey())
                .setUploadToken(preUploadToken.getUploadToken())
        ;


        return resp;
    }

    @Override
    public void uploadCallback(UploadCallbackReq req) {

        SysStorageFile storageFile = storageFileMpService.getById(req.getFileId());
        if (storageFile==null) {
            throw new RuntimeException("回调失败，无文件");
        }
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
    public FileInfo upload(String relativeDirectory,String fileName, String mimeType, InputStream inputStream) {

        //上传
        UploadBO upload = storageTemplate.upload(inputStream, relativeDirectory, fileName, mimeType);


        SysStorageFile file = new SysStorageFile()
                .setFileKey(upload.getKey())
                .setFileName(fileName)
                .setMimeType(mimeType)
                .setSize(upload.getSize())
                .setStatus(2)
                ;
        storageFileMpService.save(file);
        //存储
       return new FileInfo(file.getId(),fileName, storageTemplate.downloadUrl(upload.getKey()), upload.getSize());
    }


    @Override
    public FileInfo getDownloadInfo(Long fileId) {
        SysStorageFile storageFile = storageFileMpService.getById(fileId);
        if (storageFile ==null){
            return null;
        }
        String url = storageTemplate.downloadUrl(storageFile.getFileKey());
        return new FileInfo(storageFile.getId(), storageFile.getFileName(), url, storageFile.getSize());
    }

    @Override
    public List<FileInfo> listDownloadInfo(List<Long> fileIds) {
        List<SysStorageFile> SysStorageFiles = storageFileMpService.listByIds(fileIds);
        if (CollectionUtils.isEmpty(SysStorageFiles)) {
            return Collections.emptyList();
        }
        return SysStorageFiles.stream()
                .map(f -> new FileInfo(f.getId(), f.getFileName(), storageTemplate.downloadUrl(f.getFileKey()), f.getSize()))
                .collect(Collectors.toList());
    }

}
