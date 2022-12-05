package com.kewen.framework.boot.authority.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kewen.framework.boot.authority.biz.mapper.entity.SysDept;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SysDeptMapper extends BaseMapper<SysDept> {
    int updateBatch(List<SysDept> list);

    int updateBatchSelective(List<SysDept> list);

    int batchInsert(@Param("list") List<SysDept> list);

    int insertOrUpdate(SysDept record);

    int insertOrUpdateSelective(SysDept record);
}