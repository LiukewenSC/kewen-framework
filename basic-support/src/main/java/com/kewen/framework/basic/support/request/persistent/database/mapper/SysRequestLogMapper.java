package com.kewen.framework.basic.support.request.persistent.database.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 
 * @author kewen
 * @since 2023-04-27
 */
@Mapper
public interface SysRequestLogMapper {

    @Select("select count(1) from (select distinct  ip_address from sys_request_log where create_time between current_date and (current_date + interval 1 day)) t_temp")
    int todayIpCount();
}
