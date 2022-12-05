package com.kewen.framework.boot.authority.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kewen.framework.boot.authority.biz.mapper.entity.SysRolePermission;

import java.util.List;

import com.kewen.common.model.Permission;
import org.apache.ibatis.annotations.Param;

public interface SysRolePermissionMapper extends BaseMapper<SysRolePermission> {
    int updateBatch(List<SysRolePermission> list);

    int updateBatchSelective(List<SysRolePermission> list);

    int batchInsert(@Param("list") List<SysRolePermission> list);

    int insertOrUpdate(SysRolePermission record);

    int insertOrUpdateSelective(SysRolePermission record);

    List<Permission> listRolePosition(@Param("roleIds") List<Integer> roleIds);
}