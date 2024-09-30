package com.kewen.framework.storage.web;

import com.alibaba.fastjson2.JSONObject;
import com.kewen.framework.basic.exception.BizException;
import com.kewen.framework.basic.model.Result;
import com.kewen.framework.storage.core.model.FileInfo;
import com.kewen.framework.storage.web.model.PreUploadTokenReq;
import com.kewen.framework.storage.web.model.PreUploadTokenResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author kewen
 * @since 2023-04-24
 */
@ResponseBody
@RequestMapping("/storage")
@Slf4j
public class FileStorageEndpoint {


    @Autowired
    FileStorageProcessor fileStorageProcessor;


    /**
     * 客户端上传模式  生成token
     * @param req
     * @return
     */
    @PostMapping("/genUploadToken")
    public Result getUploadToken(@RequestBody PreUploadTokenReq req){
        log.info("上传文件获取token {}", JSONObject.toJSONString(req));
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        PreUploadTokenResp resp = fileStorageProcessor.genUploadToken(yyyyMMdd, req.getFileName());
        return Result.success(resp);
    }


    /**
     * 服务端上传
     * @param relativeDirectory
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public Result<FileInfo> upload(@RequestParam("relativeDirectory") String relativeDirectory,MultipartFile file) {
        try {
            //原始文件名
            String originalFilename = file.getOriginalFilename();

            // 文件类型 如文本(text/plain), 图片(image/png)
            String contentType = file.getContentType();

            InputStream inputStream = file.getInputStream();

            FileInfo fileInfo = fileStorageProcessor.upload(relativeDirectory,originalFilename, contentType, inputStream);

            return Result.success(fileInfo);
        } catch (IOException e) {
            throw new BizException(e);
        }
    }

    @GetMapping("/getDownloadUrl")
    public Result<FileInfo> getDownloadUrl(@RequestParam("fileId") Long fileId) {
        FileInfo result = fileStorageProcessor.getDownloadInfo(fileId);
        return Result.success(result);
    }
    @GetMapping("/listDownloadUrl")
    public Result<List<FileInfo>> listDownloadUrl(List<Long> fileIds) {
        List<FileInfo> result = fileStorageProcessor.listDownloadInfo(fileIds);
        return Result.success(result);
    }


}
