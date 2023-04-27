package com.kewen.framework.extension.sysrequest;

import org.apache.ibatis.annotations.Mapper;

/**
 * 
 * @author kewen
 * @since 2023-04-27
 */
@Mapper
public interface SysRequestLogMapper {

    int todayIpCount();
}
