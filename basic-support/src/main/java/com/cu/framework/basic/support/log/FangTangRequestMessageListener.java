package com.cu.framework.basic.support.log;

import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.cu.framework.basic.logger.request.RequestLoggerEvent;
import com.cu.framework.basic.logger.request.RequestLogger;
import com.cu.framework.basic.support.message.FangTangMessageClient;
import com.cu.framework.basic.support.message.FangTangMessageDTO;
import com.cu.framework.basic.support.utils.MarkdownUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 发送方糖消息
 *
 * @author kewen
 * @since 2024-08-29
 */
public class FangTangRequestMessageListener implements ApplicationListener<RequestLoggerEvent> {

    private static final Logger log = LoggerFactory.getLogger(FangTangRequestMessageListener.class);

    /** 已推送过的 IP 记录，键为请求来源 IP；缓存采用 expireAfterAccess(5 分钟)，即每次访问都会续期 */
    private static final Cache<String, Object> cache = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();

    /** 缓存的占位值，本场景只关心键是否存在，值本身无意义，故所有键共用同一实例 */
    private static final Object object = new Object();

    /** 方糖消息发送客户端，由外部通过 setter 注入 */
    private FangTangMessageClient fangTangMessageClient;

    /** 当前应用名，取自 spring.application.name，未配置时为空串，用于拼接消息标题 */
    @Value("${spring.application.name:}")
    String applicationName;


    public FangTangRequestMessageListener() {
        log.info("开启请求消息通知 - 方糖渠道");
    }

    /**
     * 异步接收请求日志事件，并向方糖推送一条包含本次请求详情的通知
     * <p>以来源 IP 为键做去重：仅当该 IP 不在 {@link #cache} 中时才真正发送，
     * 发送成功后写入缓存，因此同一 IP 在缓存有效期内不会重复推送。</p>
     * @param event 请求日志事件，携带本次请求采集到的日志内容
     * @throws RuntimeException 缓存加载失败时抛出（包装 {@link ExecutionException}）
     */
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
                                .setTitle("[" + applicationName + "]消息, IP["+requestLogger.getIp()+"]")
                                .setShortDesp("IP["+requestLogger.getIp()+"]发起请求")
                                .setDesp(parseMarkdown(requestLogger))
                );
                return object;
            });
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * 将一次请求的采集结果渲染成 Markdown 文本，依次包含 ip地址、url、headers（逐条 K-V）、params、body 五个大标题分节
     * @param requestLogger 本次请求采集到的日志内容
     * @return 渲染完成的 Markdown 字符串，作为方糖消息的 desp 正文
     */
    private String parseMarkdown(RequestLogger requestLogger){
        MarkdownUtil.SectionBuilder builder = MarkdownUtil.of()
                .bigTitle("ip地址")
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

        return builder.build();
    }

    public void setFangTangMessageClient(FangTangMessageClient fangTangMessageClient) {
        this.fangTangMessageClient = fangTangMessageClient;
    }
}
