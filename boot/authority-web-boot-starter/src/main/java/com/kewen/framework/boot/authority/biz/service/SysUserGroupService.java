package com.kewen.framework.boot.authority.biz.service;

import com.kewen.framework.boot.authority.biz.entity.SysUserGroup;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;


import java.util.List;


/**
 * <p>
 * 用户角色组关联表 服务类
 * </p>
 *
 * @author kewen
 * @since 2022-12-05
 */
public interface SysUserGroupService extends IService<SysUserGroup> {

        /**
         * 分页查询
         * @param pageNo 页数
         * @param pageSize 页面大小
         * @param queryModel 查询参数
         * @return Page<SysUserGroup>
         */
        Page<SysUserGroup> pageQuery(Integer pageNo, Integer pageSize, SysUserGroup queryModel);

        /**
         * 列表查询
         * @param queryModel 查询参数
         * @return List<SysUserGroup>
         */
        List<SysUserGroup> findList(SysUserGroup queryModel);
}
