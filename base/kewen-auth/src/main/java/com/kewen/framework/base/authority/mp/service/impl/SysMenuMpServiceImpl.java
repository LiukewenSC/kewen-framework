package com.kewen.framework.base.authority.mp.service.impl;

import com.kewen.framework.base.authority.mp.entity.SysMenu;
import com.kewen.framework.base.authority.mp.mapper.SysMenuMpMapper;
import com.kewen.framework.base.authority.mp.service.SysMenuMpService;
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
 * 菜单表 服务实现类
 * </p>
 *
 * @author kewen
 * @since 2023-04-07
 */
@Service
@Primary
public class SysMenuMpServiceImpl extends ServiceImpl<SysMenuMpMapper, SysMenu> implements SysMenuMpService {

}
