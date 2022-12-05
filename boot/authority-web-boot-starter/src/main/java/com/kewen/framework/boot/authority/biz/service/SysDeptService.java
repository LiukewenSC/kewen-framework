package com.kewen.framework.boot.authority.biz.service;

import com.kewen.framework.boot.authority.biz.mapper.entity.SysDept;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.IService;
public interface SysDeptService extends IService<SysDept>{


    int updateBatch(List<SysDept> list);

    int updateBatchSelective(List<SysDept> list);

    int batchInsert(List<SysDept> list);

    int insertOrUpdate(SysDept record);

    int insertOrUpdateSelective(SysDept record);

}
