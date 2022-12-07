package com.kewen.framework.base.authority.service;

import com.kewen.framework.base.authority.entity.SysRolePermission;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;


import java.util.List;


/**
 * <p>
 * 角色权限关联表 服务类
 * </p>
 *
 * @author kewen
 * @since 2022-12-07
 */
public interface SysRolePermissionService extends IService<SysRolePermission> {

        /**
         * 分页查询
         * @param pageNo 页数
         * @param pageSize 页面大小
         * @param queryModel 查询参数
         * @return Page<SysRolePermission>
         */
        Page<SysRolePermission> pageQuery(Integer pageNo, Integer pageSize, SysRolePermission queryModel);

        /**
         * 列表查询
         * @param queryModel 查询参数
         * @return List<SysRolePermission>
         */
        List<SysRolePermission> findList(SysRolePermission queryModel);
}
