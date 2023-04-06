package com.kewen.framework.base.authority.service;

import com.kewen.framework.base.authority.entity.SysGroupRole;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;


import java.util.List;


/**
 * <p>
 * 角色组角色关联表 服务类
 * </p>
 *
 * @author kewen
 * @since 2022-12-07
 */
public interface SysGroupRoleService extends IService<SysGroupRole> {

        /**
         * 分页查询
         * @param pageNo 页数
         * @param pageSize 页面大小
         * @param queryModel 查询参数
         * @return Page<SysGroupRole>
         */
        Page<SysGroupRole> pageQuery(Integer pageNo, Integer pageSize, SysGroupRole queryModel);

        /**
         * 列表查询
         * @param queryModel 查询参数
         * @return List<SysGroupRole>
         */
        List<SysGroupRole> findList(SysGroupRole queryModel);
}
