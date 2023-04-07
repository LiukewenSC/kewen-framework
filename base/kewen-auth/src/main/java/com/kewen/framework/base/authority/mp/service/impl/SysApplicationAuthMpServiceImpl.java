package com.kewen.framework.base.authority.mp.service.impl;

import com.kewen.framework.base.authority.mp.entity.SysApplicationAuth;
import com.kewen.framework.base.authority.mp.mapper.SysApplicationAuthMpMapper;
import com.kewen.framework.base.authority.mp.service.SysApplicationAuthMpService;
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
 * 应用权限表 服务实现类
 * </p>
 *
 * @author kewen
 * @since 2023-04-07
 */
@Service
@Primary
public class SysApplicationAuthMpServiceImpl extends ServiceImpl<SysApplicationAuthMpMapper, SysApplicationAuth> implements SysApplicationAuthMpService {

}