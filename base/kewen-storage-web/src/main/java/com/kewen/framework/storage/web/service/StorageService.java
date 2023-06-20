package com.kewen.framework.storage.web.service;

import com.kewen.framework.storage.core.model.FileInfo;
import com.kewen.framework.storage.core.model.PreUploadTokenBO;
import com.kewen.framework.storage.web.model.PreUploadTokenResp;
import com.kewen.framework.storage.web.model.UploadCallbackReq;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.InputStream;
import java.util.List;

/**
 * 
 * @author kewen
 * @since 2023-04-24
 */
public interface StorageService {

    /**
     * 客户端上传，获取上传token
     * @return
     */
    PreUploadTokenResp genUploadToken(String moduleName, String fileName);
    /**
     * 客户端上传回调接口
     * @param req
     */
    void uploadCallback(UploadCallbackReq req);


    /**
     *直接上传文件
     * @param moduleName  模块名
     * @param fileName  文件名
     * @param mimeType 文件类型
     * @param inputStream 文件流
     * @return
     */
    FileInfo upload(String moduleName,String fileName, String mimeType, InputStream inputStream);

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
