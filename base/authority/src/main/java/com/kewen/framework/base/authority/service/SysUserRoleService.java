package com.kewen.framework.base.authority.service;

import com.kewen.framework.base.authority.entity.SysUserRole;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kewen.framework.base.common.model.Role;


import java.util.List;


/**
 * <p>
 *  服务类
 * </p>
 *
 * @author kewen
 * @since 2022-12-07
 */
public interface SysUserRoleService extends IService<SysUserRole> {

    List<Role> listUserRole(Long userId);
}
