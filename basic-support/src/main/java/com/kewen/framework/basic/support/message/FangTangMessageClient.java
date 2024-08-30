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
 *
 * @author kewen
 * @since 2024-08-29
 */
public class FangTangMessageClient {

    private static final Logger log = LoggerFactory.getLogger(FangTangMessageClient.class);

    String key;

    String domain;



    public FangTangMessageClient() {
        log.info("开启方糖消息推送");
    }


    public void sendMessage(FangTangMessageDTO entity) {

        try {
            //这里content可以考虑弄成md或HTML格式的，方便阅读
            String url = domain + "/" + key + ".send";

            Map<Object, Object> body = MapBuilder.create()
                    .put("title", entity.getTitle())
                    .put(entity.getDesp() != null, "desp", entity.getDesp())
                    .put(entity.getShortDesp() != null, "short", entity.getShortDesp())
                    .put(entity.getNoip() != null, "noip", entity.getNoip())
                    .put(entity.getChannel() != null, "channel", entity.getChannel())
                    .put(entity.getOpenid() != null, "openid", entity.getOpenid())
                    .build();

            String result = HttpUtil.createPost(url)
                    .contentType("application/json")
                    .body(JSONObject.toJSONString(body))
                    .execute()
                    .body();
            if (log.isDebugEnabled()) {
                log.debug("返回信息:{}", result);
            }
        } catch (Throwable t) {
            log.warn("方糖服务异常: " + t.getMessage(), t);
        }
    }


    public void setKey(String key) {
        this.key = key;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }
}
