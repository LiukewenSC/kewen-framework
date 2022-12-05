package com.kewen.framework.boot.authority.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kewen.framework.boot.authority.biz.mapper.entity.SysUserDept;

import java.util.List;

import com.kewen.framework.base.common.model.DeptPrimary;
import org.apache.ibatis.annotations.Param;

public interface SysUserDeptMapper extends BaseMapper<SysUserDept> {
    int updateBatch(List<SysUserDept> list);

    int updateBatchSelective(List<SysUserDept> list);

    int batchInsert(@Param("list") List<SysUserDept> list);

    int insertOrUpdate(SysUserDept record);

    int insertOrUpdateSelective(SysUserDept record);

    /**
     * 查询用户部门
     * @param userId
     * @return
     */
    List<DeptPrimary> listUserDept(@Param("userId") Integer userId);
}