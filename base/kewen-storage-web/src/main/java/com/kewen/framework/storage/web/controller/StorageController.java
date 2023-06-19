package com.kewen.framework.storage.web.controller;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.digest.MD5;
import com.kewen.framework.common.core.model.Result;
import com.kewen.framework.common.core.utils.UUIDUtil;
import com.kewen.framework.storage.core.StorageTemplate;
import com.kewen.framework.storage.core.model.PreUploadTokenBO;
import com.kewen.framework.storage.core.model.UploadBO;
import com.kewen.framework.storage.core.model.FileInfo;
import com.kewen.framework.storage.web.service.StorageService;
import com.qiniu.util.Md5;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author kewen
 * @since 2023-04-24
 */
@RestController
@RequestMapping("/storage")
@Slf4j
public class StorageController {

    @Autowired
    StorageTemplate storageTemplate;

    @Autowired
    StorageService storageService;



    @PostMapping("/genUploadToken")
    public Result getUploadToken(@RequestParam("")String moduleName, @RequestParam("fileName") String fileName){
        log.info("上传文件获取token {}",fileName);

        PreUploadTokenBO tokenBO = storageTemplate.createPreUploadToken(fileName);

        return Result.success(tokenBO);
    }



    @PostMapping("/upload")
    public Result<FileInfo> upload(MultipartFile file) {
        try {
            //原始文件名
            String originalFilename = file.getOriginalFilename();

            // 文件类型 如文本(text/plain), 图片(image/png)
            String contentType = file.getContentType();

            InputStream inputStream = file.getInputStream();

            // 后缀，即文件类型
            String suffix;
            if (originalFilename != null && originalFilename.contains(".")) {
                suffix = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            } else {
                suffix = contentType;
            }
            String uuid = UUIDUtil.generate();
            String storageName = uuid + "." + suffix;

            //上传
            UploadBO upload = storageTemplate.upload(inputStream, storageName, contentType);

            //存储
            FileInfo fileInfo = storageService.save(originalFilename, suffix, storageName, upload.getKey(), contentType, upload.getSize());

            return Result.success(fileInfo);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/getDownloadUrl")
    public Result<FileInfo> getDownloadUrl(Long fileId) {
        FileInfo result = storageService.getDownloadInfo(fileId);
        return Result.success(result);
    }
    @GetMapping("/listDownloadUrl")
    public Result<List<FileInfo>> listDownloadUrl(List<Long> fileIds) {
        List<FileInfo> result = storageService.listDownloadInfo(fileIds);
        return Result.success(result);
    }


}