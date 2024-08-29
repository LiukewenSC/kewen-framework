package com.kewen.framework.extension.request;

import com.alibaba.fastjson.JSON;

import com.kewen.framework.basic.logger.request.PersistentRequestEvent;
import com.kewen.framework.basic.logger.request.RequestInfo;
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
public class DatabasePersistentListener implements ApplicationListener<PersistentRequestEvent> {

    SysRequestLogMpService requestLogMpService;

    @Override
    public void onApplicationEvent(PersistentRequestEvent event) {
        save(event.getRequestInfo());
    }
    public void save(RequestInfo requestInfo ) {
        SysRequestLog sysRequestLog = new SysRequestLog()
                .setMethod(requestInfo.getMethod())
                .setMillisecond(requestInfo.getExecMillisecond())
                .setIpAddress(requestInfo.getIp())
                .setIpComment(null)
                .setParams(requestInfo.getParams())
                .setBody(JSON.toJSONString(requestInfo.getBody()))
                .setUrl(requestInfo.getUrl())
                .setTraceId(TraceContext.get())
        ;
        requestLogMpService.save(sysRequestLog);
    }

    public void setRequestLogMpService(SysRequestLogMpService requestLogMpService) {
        this.requestLogMpService = requestLogMpService;
    }
}
