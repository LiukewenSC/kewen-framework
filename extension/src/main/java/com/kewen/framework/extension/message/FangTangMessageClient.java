package com.kewen.framework.extension.message;


import cn.hutool.core.map.MapBuilder;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 方糖消息发送控制器
 * @author kewen
 * @since 2024-08-29
 */
public class FangTangMessageClient {

    private static final Logger log = LoggerFactory.getLogger(FangTangMessageClient.class);

    String key = "SCT248328TA-iYbvN5Vab4ixXBUOqA60fLY5";

    String fangTangDomain="https://sctapi.ftqq.com/";

    public void sendMessage(String title, String content) {

        try {
            //这里content可以考虑弄成md或HTML格式的，方便阅读
            String url = fangTangDomain + key + ".send";
            Map<Object, Object> build = MapBuilder.create().put("title", title).put("desp", content).build();
            String body = HttpUtil.createPost(url)
                    .contentType("application/json")
                    .body(JSONObject.toJSONString(build))
                    .execute()
                    .body();
            log.info("返回信息:{}", body);
        } catch (Throwable t) {
            log.warn("方糖服务异常: " + t.getMessage(),t);
        }

    }

}
