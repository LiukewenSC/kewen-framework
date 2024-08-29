package com.kewen.framework.extension.request.message;

import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.kewen.framework.basic.logger.request.RequestLoggerEvent;
import com.kewen.framework.basic.logger.request.RequestLogger;
import com.kewen.framework.extension.message.FangTangMessageClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationListener;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 持久化日志到数据库记录
 *
 * @author kewen
 * @since 2024-08-29
 */
public class FangTangRequestMessageListener implements ApplicationListener<RequestLoggerEvent> {

    private FangTangMessageClient fangTangMessageClient;

    Cache<String, Object> cache = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();


    @Override
    public void onApplicationEvent(RequestLoggerEvent event) {
        RequestLogger requestLogger = event.getRequestLogger();
        try {
            cache.get(requestLogger.getIp(), () -> {
                //每次过期后新的请求就写一次数据
                fangTangMessageClient.sendMessage("新的IP发起请求",JSONObject.toJSONString(requestLogger));
                return true;
            });
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void setFangTangMessageClient(FangTangMessageClient fangTangMessageClient) {
        this.fangTangMessageClient = fangTangMessageClient;
    }
}
