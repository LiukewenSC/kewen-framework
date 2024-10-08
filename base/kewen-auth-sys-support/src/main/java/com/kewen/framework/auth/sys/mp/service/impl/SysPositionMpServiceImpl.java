package com.kewen.framework.auth.sys.mp.service.impl;

import com.kewen.framework.auth.sys.mp.entity.SysPosition;
import com.kewen.framework.auth.sys.mp.mapper.SysPositionMpMapper;
import com.kewen.framework.auth.sys.mp.service.SysPositionMpService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 岗位表 服务实现类
 * </p>
 *
 * @author kewen
 * @since 2023-04-07
 */
@Service
@Primary
public class SysPositionMpServiceImpl extends ServiceImpl<SysPositionMpMapper, SysPosition> implements SysPositionMpService {

}
