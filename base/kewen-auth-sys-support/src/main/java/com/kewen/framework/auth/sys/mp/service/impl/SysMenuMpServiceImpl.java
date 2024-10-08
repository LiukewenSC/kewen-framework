package com.kewen.framework.auth.sys.mp.service.impl;

import com.kewen.framework.auth.sys.mp.entity.SysMenu;
import com.kewen.framework.auth.sys.mp.mapper.SysMenuMpMapper;
import com.kewen.framework.auth.sys.mp.service.SysMenuMpService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

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
