package com.kewen.framework.storage.web;

import com.alibaba.fastjson.JSONObject;
import com.kewen.framework.basic.model.Result;
import com.kewen.framework.storage.web.model.UploadCallbackReq;
import com.kewen.framework.storage.web.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件上传完成回调
 * @author kewen
 * @since 2023-06-19
 */
@RestController()
@RequestMapping("/storage")
@Slf4j
public class StorageUploadCallback {

    @Autowired
    StorageService storageService;

    /**
     * 文件上传回调函数
     * @param req
     * @return
     */
    @RequestMapping("/upload/callback")
    public Result uploadCallback(@RequestBody UploadCallbackReq req){

        log.info("上传文件回调req: "+ JSONObject.toJSONString(req));

        storageService.uploadCallback(req);

        return Result.success();
    }

}
