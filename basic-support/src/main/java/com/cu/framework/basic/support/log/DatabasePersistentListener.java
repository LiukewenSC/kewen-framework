package com.cu.framework.basic.support.log;

import com.alibaba.fastjson.JSON;

import com.cu.framework.basic.logger.request.RequestLoggerEvent;
import com.cu.framework.basic.logger.request.RequestLogger;
import com.cu.framework.basic.logger.trace.TraceContext;
import com.cu.framework.basic.support.mp.entity.SysRequestLog;
import com.cu.framework.basic.support.mp.service.SysRequestLogMpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;

/**
 * 持久化日志到数据库记录
 *
 * @author kewen
 * @since 2023-04-26
 */
public class DatabasePersistentListener implements ApplicationListener<RequestLoggerEvent> {

    private static final Logger log = LoggerFactory.getLogger(DatabasePersistentListener.class);

    /** 请求日志表的 MyBatis-Plus 服务，由外部通过 setter 注入 */
    SysRequestLogMpService requestLogMpService;

    public DatabasePersistentListener() {
        log.info("开启请求日志数据库持久化");
    }

    /**
     * 异步接收请求日志事件并落库，不阻塞请求线程
     * @param event 请求日志事件，携带本次请求采集到的日志内容
     */
    @Override
    @Async
    public void onApplicationEvent(RequestLoggerEvent event) {
        RequestLogger requestLogger = event.getRequestLogger();
        if (log.isDebugEnabled()) {
            log.debug("请求日志数据库持久化,{}", JSON.toJSONString(requestLogger));
        }
        save(requestLogger);
    }
    /**
     * 将一次请求的采集结果转换为 sys_request_log 实体并保存
     * <p>其中 body 以 JSON 字符串形式入库，traceId 取自当前线程的 {@link TraceContext}</p>
     * @param requestLogger 本次请求采集到的日志内容
     */
    public void save(RequestLogger requestLogger) {
        SysRequestLog sysRequestLog = new SysRequestLog()
                .setMethod(requestLogger.getMethod())
                .setMillisecond(requestLogger.getExecMillisecond())
                .setIpAddress(requestLogger.getIp())
                .setIpComment(null)
                .setParams(requestLogger.getParams())
                .setBody(JSON.toJSONString(requestLogger.getBody()))
                .setApi_path(requestLogger.getUrl())
                .setTraceId(TraceContext.get())
        ;
        requestLogMpService.save(sysRequestLog);
    }

    public void setRequestLogMpService(SysRequestLogMpService requestLogMpService) {
        this.requestLogMpService = requestLogMpService;
    }
}
