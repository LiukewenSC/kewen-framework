package com.kewen.framework.basic.support.log.persistent;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;

/**
 * 
 * @author kewen
 * @since 2023-04-27
 */
@Data
@Accessors(chain = true)
public class TodayStatisticResp {
    private LocalDate day;
    private Integer ipCount;
    private Integer userCount;

}
