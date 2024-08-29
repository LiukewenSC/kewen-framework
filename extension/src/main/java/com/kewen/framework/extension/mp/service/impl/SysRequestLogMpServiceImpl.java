package com.kewen.framework.extension.mp.service.impl;

import com.kewen.framework.extension.mp.entity.SysRequestLog;
import com.kewen.framework.extension.mp.mapper.SysRequestLogMpMapper;
import com.kewen.framework.extension.mp.service.SysRequestLogMpService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
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
