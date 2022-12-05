package com.kewen.framework.boot.authority.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kewen.framework.boot.authority.biz.mapper.entity.SysPosition;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SysPositionMapper extends BaseMapper<SysPosition> {
    int updateBatch(List<SysPosition> list);

    int updateBatchSelective(List<SysPosition> list);

    int batchInsert(@Param("list") List<SysPosition> list);

    int insertOrUpdate(SysPosition record);

    int insertOrUpdateSelective(SysPosition record);
}