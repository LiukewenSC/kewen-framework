package com.kewen.framework.basic.support.message;


import cn.hutool.core.map.MapBuilder;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

/**
 * 方糖消息发送控制器
 * @author kewen
 * @since 2024-08-29
 */
public class FangTangMessageClient {

    private static final Logger log = LoggerFactory.getLogger(FangTangMessageClient.class);

    String key ;

    String domain;

    @Value("${spring.application.name}")
    String applicationName;

    public FangTangMessageClient() {
        log.info("开启方糖消息推送");
    }

    public void sendMessage(String title, String content) {

        try {
            //这里content可以考虑弄成md或HTML格式的，方便阅读
            String url = domain +"/" + key + ".send";
            Map<Object, Object> build = MapBuilder.create().put("title", appendTitle(title)).put("desp", content).build();
            String body = HttpUtil.createPost(url)
                    .contentType("application/json")
                    .body(JSONObject.toJSONString(build))
                    .execute()
                    .body();
            if (log.isDebugEnabled()) {
                log.debug("返回信息:{}", body);
            }
        } catch (Throwable t) {
            log.warn("方糖服务异常: " + t.getMessage(),t);
        }
    }

    private String appendTitle(String title) {
        return applicationName +"工程消息: " +title;
    }


    public void setKey(String key) {
        this.key = key;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }
}
