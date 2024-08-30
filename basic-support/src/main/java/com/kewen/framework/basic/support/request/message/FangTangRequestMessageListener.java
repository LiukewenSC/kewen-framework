package com.kewen.framework.basic.support.request.message;

import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.kewen.framework.basic.logger.request.RequestLoggerEvent;
import com.kewen.framework.basic.logger.request.RequestLogger;
import com.kewen.framework.basic.support.message.FangTangMessageClient;
import com.kewen.framework.basic.support.message.FangTangMessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
    private static final Cache<String, Object> cache = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();
    private static final Object object = new Object();

    private FangTangMessageClient fangTangMessageClient;

    @Value("${spring.application.name}")
    String applicationName;


    public FangTangRequestMessageListener() {
        log.info("开启请求消息通知 - 方糖渠道");
    }

    @Override
    @Async
    public void onApplicationEvent(RequestLoggerEvent event) {

        RequestLogger requestLogger = event.getRequestLogger();

        if (log.isDebugEnabled()) {
            log.debug("发送方糖消息通知: {}",JSONObject.toJSONString(requestLogger));
        }

        try {
            cache.get(requestLogger.getIp(), () -> {
                //每次过期后新请求需要写一次数据
                fangTangMessageClient.sendMessage(
                        new FangTangMessageDTO()
                                .setTitle("[" + applicationName + "]消息")
                                .setShortDesp("IP["+requestLogger.getIp()+"]发起请求")
                                .setDesp(JSONObject.toJSONString(requestLogger))
                );
                return object;
            });
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void setFangTangMessageClient(FangTangMessageClient fangTangMessageClient) {
        this.fangTangMessageClient = fangTangMessageClient;
    }
}
