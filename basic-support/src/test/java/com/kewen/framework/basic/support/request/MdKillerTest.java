package com.kewen.framework.basic.support.request;


import cn.hutool.core.map.MapBuilder;
import com.alibaba.fastjson.JSONObject;
import com.kewen.framework.basic.logger.request.RequestLogger;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class MdKillerTest {

    @Test
    public void t1(){
        MapBuilder<String, String> headers = new MapBuilder<>(new HashMap<>());
        headers.put("token","token11")
                .put("tenant","租户1")
                .put("contentType","application/json");
        JSONObject body = new JSONObject();
        body.put("username","name1");
        body.put("password","123456");

        RequestLogger requestLogger = new RequestLogger()
                .setIp(":127.0.0.1")
                .setUrl("url")
                .setHeaders(headers.build())
                .setBody(body)
                .setParams("id=1&name=name1")
                .setExecMillisecond(2000);

        MdUtil.SectionBuilder builder = MdUtil.of()
                .bigTitle("Ip地址")
                .text(requestLogger.getIp())
                .bigTitle("url")
                .text(requestLogger.getUrl());
        builder.bigTitle("headers");
        for (Map.Entry<String, String> entry : requestLogger.getHeaders().entrySet()) {
            builder.text(entry.getKey(),entry.getValue());
        }
        builder.br();
        builder.bigTitle("params")
                .text(requestLogger.getParams());
        builder.bigTitle("body")
                .text(requestLogger.getBody().toString());

        System.out.println(builder.build());
    }

}