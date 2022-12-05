package com.kewen.framework.boot.authority.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kewen.framework.boot.authority.biz.mapper.entity.SysGroupRole;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SysGroupRoleMapper extends BaseMapper<SysGroupRole> {
    int updateBatch(List<SysGroupRole> list);

    int updateBatchSelective(List<SysGroupRole> list);

    int batchInsert(@Param("list") List<SysGroupRole> list);

    int insertOrUpdate(SysGroupRole record);

    int insertOrUpdateSelective(SysGroupRole record);
}