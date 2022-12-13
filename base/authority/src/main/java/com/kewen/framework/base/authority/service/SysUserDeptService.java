package com.kewen.framework.base.authority.service;

import com.kewen.framework.base.authority.entity.SysUserDept;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kewen.framework.base.common.model.UserDept;


import java.util.List;


/**
 * <p>
 * 用户部门关联表 服务类
 * </p>
 *
 * @author kewen
 * @since 2022-12-07
 */
public interface SysUserDeptService extends IService<SysUserDept> {

    /**
     * 分页查询
     * @param pageNo 页数
     * @param pageSize 页面大小
     * @param queryModel 查询参数
     * @return Page<SysUserDept>
     */
    Page<SysUserDept> pageQuery(Integer pageNo, Integer pageSize, SysUserDept queryModel);

    /**
     * 列表查询
     * @param queryModel 查询参数
     * @return List<SysUserDept>
     */
    List<SysUserDept> findList(SysUserDept queryModel);

    UserDept getUserDept(Long userId);
}
