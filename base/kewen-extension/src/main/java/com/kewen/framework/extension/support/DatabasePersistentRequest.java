package com.kewen.framework.extension.support;

import com.alibaba.fastjson.JSON;
import com.kewen.framework.common.logger.context.TraceContext;
import com.kewen.framework.common.logger.model.RequestInfo;
import com.kewen.framework.common.logger.PersistentRequestStore;
import com.kewen.framework.extension.mp.entity.SysRequestLog;
import com.kewen.framework.extension.mp.service.SysRequestLogMpService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 持久化日志到数据库记录
 *
 * @author kewen
 * @since 2023-04-26
 */
public class DatabasePersistentRequest implements PersistentRequestStore {

    @Autowired
    SysRequestLogMpService requestLogMpService;

    @Override
    public void save(RequestInfo requestInfo, Integer millisecond) {
        SysRequestLog sysRequestLog = new SysRequestLog()
                .setMethod(requestInfo.getMethod())
                .setMillisecond(millisecond)
                .setIpAddress(requestInfo.getIp())
                .setIpComment(null)
                .setParams(requestInfo.getParams())
                .setBody(JSON.toJSONString(requestInfo.getBody()))
                .setUrl(requestInfo.getUrl())
                .setTraceId(TraceContext.get())
        ;
        requestLogMpService.save(sysRequestLog);
    }
}
