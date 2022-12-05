package com.kewen.framework.boot.authority.biz.service;

import java.util.List;
import com.kewen.framework.boot.authority.biz.mapper.entity.SysRolePermission;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kewen.common.model.Permission;

public interface SysRolePermissionService extends IService<SysRolePermission>{


    int updateBatch(List<SysRolePermission> list);

    int updateBatchSelective(List<SysRolePermission> list);

    int batchInsert(List<SysRolePermission> list);

    int insertOrUpdate(SysRolePermission record);

    int insertOrUpdateSelective(SysRolePermission record);

    List<Permission> listRolePosition(List<Integer> roleIds);
}
