package com.kewen.framework.basic.support.log.persistent;

import com.alibaba.fastjson.JSON;

import com.kewen.framework.basic.logger.request.RequestLoggerEvent;
import com.kewen.framework.basic.logger.request.RequestLogger;
import com.kewen.framework.basic.logger.trace.TraceContext;
import com.kewen.framework.basic.support.mp.entity.SysRequestLog;
import com.kewen.framework.basic.support.mp.service.SysRequestLogMpService;
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
    SysRequestLogMpService requestLogMpService;

    public DatabasePersistentListener() {
        log.info("开启请求日志数据库持久化");
    }

    @Override
    @Async
    public void onApplicationEvent(RequestLoggerEvent event) {
        RequestLogger requestLogger = event.getRequestLogger();
        if (log.isDebugEnabled()) {
            log.debug("请求日志数据库持久化,{}", JSON.toJSONString(requestLogger));
        }
        save(requestLogger);
    }
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
