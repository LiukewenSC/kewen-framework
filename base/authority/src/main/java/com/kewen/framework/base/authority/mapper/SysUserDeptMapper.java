package com.kewen.framework.base.authority.mapper;

import com.kewen.framework.base.authority.entity.SysUserDept;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kewen.framework.base.common.model.DeptPrimary;

import java.util.List;

/**
 * <p>
 * 用户部门关联表 Mapper 接口
 * </p>
 *
 * @author kewen
 * @since 2022-12-07
 */
public interface SysUserDeptMapper extends BaseMapper<SysUserDept> {

    List<DeptPrimary> listUserDept(Long userId);
}
