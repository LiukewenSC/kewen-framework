package com.kewen.framework.base.authority.mp.service.impl;

import com.kewen.framework.base.authority.mp.entity.SysPosition;
import com.kewen.framework.base.authority.mp.mapper.SysPositionMapper;
import com.kewen.framework.base.authority.mp.service.SysPositionMpService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
/**
 * <p>
 * 岗位表 服务实现类
 * </p>
 *
 * @author kewen
 * @since 2023-04-06
 */
@Service
@Primary
public class SysPositionMpServiceImpl extends ServiceImpl<SysPositionMapper, SysPosition> implements SysPositionMpService {

}
