package com.kewen.framework.boot.authority.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kewen.framework.boot.authority.biz.mapper.entity.SysGroup;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SysGroupMapper extends BaseMapper<SysGroup> {
    int updateBatch(List<SysGroup> list);

    int updateBatchSelective(List<SysGroup> list);

    int batchInsert(@Param("list") List<SysGroup> list);

    int insertOrUpdate(SysGroup record);

    int insertOrUpdateSelective(SysGroup record);
}