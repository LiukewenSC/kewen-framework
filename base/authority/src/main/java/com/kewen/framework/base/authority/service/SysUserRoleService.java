package com.kewen.framework.base.authority.service;

import com.kewen.framework.base.authority.entity.SysUserRole;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;


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

        /**
         * 分页查询
         * @param pageNo 页数
         * @param pageSize 页面大小
         * @param queryModel 查询参数
         * @return Page<SysUserRole>
         */
        Page<SysUserRole> pageQuery(Integer pageNo, Integer pageSize, SysUserRole queryModel);

        /**
         * 列表查询
         * @param queryModel 查询参数
         * @return List<SysUserRole>
         */
        List<SysUserRole> findList(SysUserRole queryModel);
}
