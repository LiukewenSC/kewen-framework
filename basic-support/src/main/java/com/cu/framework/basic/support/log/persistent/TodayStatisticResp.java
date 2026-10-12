package com.cu.framework.basic.support.log.persistent;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;

/**
 * 按天粒度的访问统计结果，由 {@link RequestLogService#visitStatistic} 基于请求日志表汇总得到
 * @author kewen
 * @since 2023-04-27
 */
@Data
@Accessors(chain = true)
public class TodayStatisticResp {
    /** 统计所属的日期 */
    private LocalDate day;
    /** 当日去重后的访问 IP 数 */
    private Integer ipCount;
    /** 当日去重后的访问用户数 */
    private Integer userCount;

}
