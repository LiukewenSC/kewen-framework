package com.kewen.framework.storage.web.controller;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.kewen.framework.common.core.model.Result;
import com.kewen.framework.storage.web.model.UploadCallbackReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author kewen
 * @since 2023-06-19
 */
@RestController()
@RequestMapping("/storage")
@Slf4j
public class StorageUploadCallback {

    @RequestMapping("/upload/callback")
    public Result uploadCallback(@RequestBody UploadCallbackReq req){

        log.info("上传文件回调req: "+ JSONObject.toJSONString(req));

        return Result.success();
    }

}
