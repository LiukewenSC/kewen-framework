package com.kewen.framework.base.authority.mp.service.impl;

import com.kewen.framework.base.authority.mp.entity.SysRolePermission;
import com.kewen.framework.base.authority.mp.mapper.SysRolePermissionMapper;
import com.kewen.framework.base.authority.mp.service.SysRolePermissionMpService;
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
 * 角色权限关联表 服务实现类
 * </p>
 *
 * @author kewen
 * @since 2023-04-06
 */
@Service
@Primary
public class SysRolePermissionMpServiceImpl extends ServiceImpl<SysRolePermissionMapper, SysRolePermission> implements SysRolePermissionMpService {

}
