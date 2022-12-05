package com.kewen.framework.boot.authority.biz.service;

import com.kewen.framework.boot.authority.biz.entity.SysPermission;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;


import java.util.List;


/**
 * <p>
 * 权限表 服务类
 * </p>
 *
 * @author kewen
 * @since 2022-12-05
 */
public interface SysPermissionService extends IService<SysPermission> {

        /**
         * 分页查询
         * @param pageNo 页数
         * @param pageSize 页面大小
         * @param queryModel 查询参数
         * @return Page<SysPermission>
         */
        Page<SysPermission> pageQuery(Integer pageNo, Integer pageSize, SysPermission queryModel);

        /**
         * 列表查询
         * @param queryModel 查询参数
         * @return List<SysPermission>
         */
        List<SysPermission> findList(SysPermission queryModel);
}
