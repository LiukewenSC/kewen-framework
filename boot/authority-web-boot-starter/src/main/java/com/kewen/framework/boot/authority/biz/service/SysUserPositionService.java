package com.kewen.framework.boot.authority.biz.service;

import java.util.List;
import com.kewen.framework.boot.authority.biz.mapper.entity.SysUserPosition;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kewen.common.model.Position;

public interface SysUserPositionService extends IService<SysUserPosition>{

    List<Position> listUserPosition(Integer userId);

    int updateBatch(List<SysUserPosition> list);

    int updateBatchSelective(List<SysUserPosition> list);

    int batchInsert(List<SysUserPosition> list);

    int insertOrUpdate(SysUserPosition record);

    int insertOrUpdateSelective(SysUserPosition record);

}
