package com.kewen.framework.basic.support.mp.mapper;

import com.kewen.framework.basic.support.mp.entity.SysRequestLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 请求日志记录器 Mapper 接口
 * </p>
 *
 * @author kewen
 * @since 2023-04-26
 */
@Mapper
public interface SysRequestLogMpMapper extends BaseMapper<SysRequestLog> {

}
