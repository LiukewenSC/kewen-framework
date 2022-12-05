package com.kewen.framework.boot.authority.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kewen.framework.boot.authority.biz.mapper.entity.SysUserPosition;

import java.util.List;

import com.kewen.framework.base.common.model.Position;
import org.apache.ibatis.annotations.Param;

public interface SysUserPositionMapper extends BaseMapper<SysUserPosition> {
    int updateBatch(List<SysUserPosition> list);

    int updateBatchSelective(List<SysUserPosition> list);

    int batchInsert(@Param("list") List<SysUserPosition> list);

    int insertOrUpdate(SysUserPosition record);

    int insertOrUpdateSelective(SysUserPosition record);

    /**
     * 查询用户岗位列表
     * @param userId
     * @return
     */
    List<Position> listUserPosition(@Param("userId") Integer userId);
}