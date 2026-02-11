package com.kewen.framework.storage.web;

import com.alibaba.fastjson.JSONObject;
import com.kewen.framework.basic.model.Result;
import com.kewen.framework.storage.core.model.FileInfo;
import com.kewen.framework.storage.web.model.UploadCallbackReq;
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
