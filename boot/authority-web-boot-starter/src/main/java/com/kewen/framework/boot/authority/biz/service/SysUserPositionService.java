package com.kewen.framework.boot.authority.biz.service;

import com.kewen.framework.boot.authority.biz.entity.SysUserPosition;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;


import java.util.List;


/**
 * <p>
 * 用户岗位关联表 服务类
 * </p>
 *
 * @author kewen
 * @since 2022-12-05
 */
public interface SysUserPositionService extends IService<SysUserPosition> {

        /**
         * 分页查询
         * @param pageNo 页数
         * @param pageSize 页面大小
         * @param queryModel 查询参数
         * @return Page<SysUserPosition>
         */
        Page<SysUserPosition> pageQuery(Integer pageNo, Integer pageSize, SysUserPosition queryModel);

        /**
         * 列表查询
         * @param queryModel 查询参数
         * @return List<SysUserPosition>
         */
        List<SysUserPosition> findList(SysUserPosition queryModel);
}
