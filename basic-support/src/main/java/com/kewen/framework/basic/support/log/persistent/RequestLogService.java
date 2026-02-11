package com.kewen.framework.basic.support.log.persistent;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kewen.framework.basic.support.mp.entity.SysRequestLog;
import com.kewen.framework.basic.support.mp.service.SysRequestLogMpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 
 * @author kewen
 * @since 2023-04-27
 */
@Service
public class RequestLogService {

    @Autowired
    SysRequestLogMpService sysRequestLogMpService;

    public List<TodayStatisticResp> visitStatistic(LocalDate startDate, LocalDate endDate){
        List<SysRequestLog> logList = sysRequestLogMpService.list(
                new LambdaQueryWrapper<SysRequestLog>()
                        .between(SysRequestLog::getCreateTime, startDate.atTime(LocalTime.MIDNIGHT), endDate.plusDays(1).atTime(LocalTime.MIDNIGHT))
                        .select(SysRequestLog::getUserId,SysRequestLog::getIpAddress,SysRequestLog::getCreateTime)
        );
        if (logList == null){
            return Collections.emptyList();
        }

        //按天分组
        Map<LocalDate, List<SysRequestLog>> requestLogMap = logList.stream().collect(Collectors.groupingBy(r -> r.getCreateTime().toLocalDate()));

        List<TodayStatisticResp> resps = new ArrayList<>();

        for (Map.Entry<LocalDate, List<SysRequestLog>> entry : requestLogMap.entrySet()) {
            List<SysRequestLog> requestLogs = entry.getValue();
            int ipCount = (int)requestLogs.stream().map(SysRequestLog::getIpAddress).distinct().count();
            int userCount = (int)requestLogs.stream().map(SysRequestLog::getUserId).distinct().count();
            resps.add(new TodayStatisticResp().setDay(entry.getKey()).setIpCount(ipCount).setUserCount(userCount));
        }
        return resps;
    }

}
