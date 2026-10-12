package com.cu.framework.storage.web;

import com.alibaba.fastjson.JSONObject;
import com.cu.framework.basic.model.Result;
import com.cu.framework.storage.core.model.FileInfo;
import com.cu.framework.storage.web.model.UploadCallbackReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 文件上传完成回调
 *
 * @author kewen
 * @since 2023-06-19
 */
@RestController()
@RequestMapping("/storage")
@Slf4j
public class FileStorageCallbackEndpoint {

    @Autowired
    FileStorageProcessor fileStorageProcessor;

    /**
     * 文件上传回调函数
     *
     * @param req
     * @return
     */
    @RequestMapping("/upload/callback")
    public Result uploadCallback(@RequestBody UploadCallbackReq req) {

        log.info("上传文件回调req: " + JSONObject.toJSONString(req));

        fileStorageProcessor.uploadCallback(req);


        return Result.success(req);
    }
    /**
     * 根据文件 ID 查询文件下载信息（GET /storage/getFileById）
     * @param id 文件 ID
     * @return 包含文件下载信息的成功结果
     */
    @GetMapping("/getFileById")
    public Result getFileById(@RequestParam Long id){
        FileInfo downloadInfo = fileStorageProcessor.getDownloadInfo(id);
        return Result.success(downloadInfo);
    }

    /**
     * 文件上传回调函数
     *
     * @param req
     * @return
     */
    @PostMapping("/upload/callbackAsync")
    public Result uploadCallbackAsync(@RequestBody Map<String, Object> req) {

        log.info("上传文件回调异步req: " + JSONObject.toJSONString(req));

        //fileStorageProcessor.uploadCallback(req);

        return Result.success();
    }

}
