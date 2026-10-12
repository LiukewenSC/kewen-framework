package com.cu.framework.basic.support.mp.service.impl;

import com.cu.framework.basic.support.mp.entity.SysRequestLog;
import com.cu.framework.basic.support.mp.mapper.SysRequestLogMpMapper;
import com.cu.framework.basic.support.mp.service.SysRequestLogMpService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 请求日志记录器 服务实现类
 * </p>
 *
 * @author kewen
 * @since 2023-04-26
 */
@Service
@Primary
public class SysRequestLogMpServiceImpl extends ServiceImpl<SysRequestLogMpMapper, SysRequestLog> implements SysRequestLogMpService {

}
