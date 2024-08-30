package com.kewen.framework.basic.support.request.message;

import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.kewen.framework.basic.logger.request.RequestLoggerEvent;
import com.kewen.framework.basic.logger.request.RequestLogger;
import com.kewen.framework.basic.support.message.FangTangMessageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 持久化日志到数据库记录
 *
 * @author kewen
 * @since 2024-08-29
 */
public class FangTangRequestMessageListener implements ApplicationListener<RequestLoggerEvent> {

    private static final Logger log = LoggerFactory.getLogger(FangTangRequestMessageListener.class);
    private FangTangMessageClient fangTangMessageClient;

    Cache<String, Object> cache = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();

    public FangTangRequestMessageListener() {
        log.info("开启请求消息通知 - 方糖渠道");
    }

    @Override
    @Async
    public void onApplicationEvent(RequestLoggerEvent event) {
        if (log.isDebugEnabled()) {
            log.debug("发送方糖消息通知: {}",JSONObject.toJSONString(event));
        }
        RequestLogger requestLogger = event.getRequestLogger();
        try {
            cache.get(requestLogger.getIp(), () -> {
                //每次过期后新的请求就写一次数据
                fangTangMessageClient.sendMessage("新的IP["+requestLogger.getIp()+"]发起请求",JSONObject.toJSONString(requestLogger));
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
