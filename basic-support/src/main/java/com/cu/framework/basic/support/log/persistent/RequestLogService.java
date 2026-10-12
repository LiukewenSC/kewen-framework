package com.cu.framework.basic.support.log.persistent;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cu.framework.basic.support.mp.entity.SysRequestLog;
import com.cu.framework.basic.support.mp.service.SysRequestLogMpService;
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
 * 请求日志统计服务，基于 sys_request_log 表汇总访问量数据
 * @author kewen
 * @since 2023-04-27
 */
@Service
public class RequestLogService {

    @Autowired
    SysRequestLogMpService sysRequestLogMpService;

    /**
     * 统计日期区间内每一天的独立访问 IP 数与独立访问用户数
     * <p>区间为闭区间：查询条件为 createTime ∈ [startDate 00:00, endDate+1 天 00:00)，因此 endDate 当天完整包含在内。
     * 返回结果按天分组，日志表中没有记录的日期不会出现在结果里。</p>
     * @param startDate 起始日期（含）
     * @param endDate 结束日期（含）
     * @return 每天的统计结果；区间内无任何日志时返回空列表
     */
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
