package com.kewen.framework.extension.request.persistent;

import com.alibaba.fastjson.JSON;

import com.kewen.framework.basic.logger.request.RequestLoggerEvent;
import com.kewen.framework.basic.logger.request.RequestLogger;
import com.kewen.framework.basic.logger.trace.TraceContext;
import com.kewen.framework.extension.mp.entity.SysRequestLog;
import com.kewen.framework.extension.mp.service.SysRequestLogMpService;
import org.springframework.context.ApplicationListener;

/**
 * 持久化日志到数据库记录
 *
 * @author kewen
 * @since 2023-04-26
 */
public class DatabasePersistentListener implements ApplicationListener<RequestLoggerEvent> {

    SysRequestLogMpService requestLogMpService;

    @Override
    public void onApplicationEvent(RequestLoggerEvent event) {
        save(event.getRequestLogger());
    }
    public void save(RequestLogger requestLogger) {
        SysRequestLog sysRequestLog = new SysRequestLog()
                .setMethod(requestLogger.getMethod())
                .setMillisecond(requestLogger.getExecMillisecond())
                .setIpAddress(requestLogger.getIp())
                .setIpComment(null)
                .setParams(requestLogger.getParams())
                .setBody(JSON.toJSONString(requestLogger.getBody()))
                .setUrl(requestLogger.getUrl())
                .setTraceId(TraceContext.get())
        ;
        requestLogMpService.save(sysRequestLog);
    }

    public void setRequestLogMpService(SysRequestLogMpService requestLogMpService) {
        this.requestLogMpService = requestLogMpService;
    }
}
