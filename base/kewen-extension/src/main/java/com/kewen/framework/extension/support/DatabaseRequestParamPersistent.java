package com.kewen.framework.extension.support;

import com.alibaba.fastjson.JSON;
import com.kewen.framework.common.context.TraceContext;
import com.kewen.framework.common.context.UserContext;
import com.kewen.framework.common.core.model.IUser;
import com.kewen.framework.common.web.context.RequestInfo;
import com.kewen.framework.common.web.filter.support.RequestParamPersistentHandler;
import com.kewen.framework.extension.mp.entity.SysRequestLog;
import com.kewen.framework.extension.mp.service.SysRequestLogMpService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 持久化日志到数据库记录
 *
 * @author kewen
 * @since 2023-04-26
 */
public class DatabaseRequestParamPersistent implements RequestParamPersistentHandler {

    @Autowired
    SysRequestLogMpService requestLogMpService;

    @Override
    public void save(RequestInfo requestInfo, Integer millisecond) {
        SysRequestLog sysRequestLog = new SysRequestLog();
        if (UserContext.support() && UserContext.get() != null) {
            IUser user = UserContext.get();
            sysRequestLog
                    .setUserId(user.getUserId())
                    .setUserName(user.getUserName());

        } else {
            sysRequestLog.setUserId(-1L);
            sysRequestLog.setUserName("anonymous");
        }

        sysRequestLog
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
