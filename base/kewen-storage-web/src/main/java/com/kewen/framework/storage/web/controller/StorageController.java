package com.kewen.framework.storage.web.controller;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.digest.MD5;
import com.alibaba.fastjson2.JSONObject;
import com.kewen.framework.common.core.exception.BizException;
import com.kewen.framework.common.core.model.Result;
import com.kewen.framework.common.core.utils.UUIDUtil;
import com.kewen.framework.storage.core.StorageTemplate;
import com.kewen.framework.storage.core.model.PreUploadTokenBO;
import com.kewen.framework.storage.core.model.UploadBO;
import com.kewen.framework.storage.core.model.FileInfo;
import com.kewen.framework.storage.web.model.PreUploadTokenReq;
import com.kewen.framework.storage.web.model.PreUploadTokenResp;
import com.kewen.framework.storage.web.mp.entity.SysStorageModule;
import com.kewen.framework.storage.web.mp.service.SysStorageModuleMpService;
import com.kewen.framework.storage.web.service.StorageService;
import com.qiniu.util.Md5;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author kewen
 * @since 2023-04-24
 */
@ResponseBody
@RequestMapping("/storage")
@Slf4j
public class StorageController {


    @Autowired
    StorageService storageService;


    /**
     * 客户端上传模式  生成token
     * @param moduleName
     * @param fileName
     * @return
     */
    @PostMapping("/genUploadToken")
    public Result getUploadToken(@RequestBody PreUploadTokenReq req){
        log.info("上传文件获取token {}", JSONObject.toJSONString(req));
        PreUploadTokenResp resp = storageService.genUploadToken(req.getModuleName(), req.getFileName());
        return Result.success(resp);
    }


    /**
     * 服务端上传
     * @param moduleName
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public Result<FileInfo> upload(@RequestParam("moduleName") String moduleName,MultipartFile file) {
        try {
            //原始文件名
            String originalFilename = file.getOriginalFilename();

            // 文件类型 如文本(text/plain), 图片(image/png)
            String contentType = file.getContentType();

            InputStream inputStream = file.getInputStream();

            FileInfo fileInfo = storageService.upload(moduleName,originalFilename, contentType, inputStream);

            return Result.success(fileInfo);
        } catch (IOException e) {
            throw new BizException(e);
        }
    }

    @GetMapping("/getDownloadUrl")
    public Result<FileInfo> getDownloadUrl(@RequestParam("fileId") Long fileId) {
        FileInfo result = storageService.getDownloadInfo(fileId);
        return Result.success(result);
    }
    @GetMapping("/listDownloadUrl")
    public Result<List<FileInfo>> listDownloadUrl(List<Long> fileIds) {
        List<FileInfo> result = storageService.listDownloadInfo(fileIds);
        return Result.success(result);
    }


}
