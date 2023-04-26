package com.kewen.framework.extension;

import com.kewen.framework.common.context.UserContext;
import com.kewen.framework.common.core.model.IUser;
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
    public void save(String url, String method, String remoteAddr, String params, String body, Integer millisecond) {
        SysRequestLog sysRequestLog = new SysRequestLog();
        if (UserContext.support()) {
            IUser user = UserContext.get();
            if (user != null) {
                sysRequestLog
                        .setUserId(user.getUserId())
                        .setUserName(user.getUserName());
            }
        }

        sysRequestLog
                .setMethod(method)
                .setMillisecond(millisecond)
                .setIpAddress(remoteAddr)
                .setIpComment(null)
                .setParams(params)
                .setBody(body)
                .setUrl(url);
        requestLogMpService.save(sysRequestLog);
    }
}
