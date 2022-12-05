package com.kewen.framework.boot.authority.biz.service;

import java.util.List;
import com.kewen.framework.boot.authority.biz.mapper.entity.SysGroupRole;
import com.baomidou.mybatisplus.extension.service.IService;
public interface SysGroupRoleService extends IService<SysGroupRole>{


    int updateBatch(List<SysGroupRole> list);

    int updateBatchSelective(List<SysGroupRole> list);

    int batchInsert(List<SysGroupRole> list);

    int insertOrUpdate(SysGroupRole record);

    int insertOrUpdateSelective(SysGroupRole record);

}
