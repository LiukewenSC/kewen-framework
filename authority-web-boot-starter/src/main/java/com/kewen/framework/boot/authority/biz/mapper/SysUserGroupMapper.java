package com.kewen.framework.boot.authority.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kewen.framework.boot.authority.biz.mapper.entity.SysUserGroup;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SysUserGroupMapper extends BaseMapper<SysUserGroup> {
    int updateBatch(List<SysUserGroup> list);

    int updateBatchSelective(List<SysUserGroup> list);

    int batchInsert(@Param("list") List<SysUserGroup> list);

    int insertOrUpdate(SysUserGroup record);

    int insertOrUpdateSelective(SysUserGroup record);
}