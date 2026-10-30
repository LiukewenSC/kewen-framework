package com.cu.framework.sample.mp.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.cu.framework.basic.model.PageReq;
import com.cu.framework.basic.model.PageResult;
import com.cu.framework.sample.mp.entity.SampleBusiness;

import java.util.List;

/**
 * 示例业务数据服务。
 *
 * @author kewen
 * @since 2026-10-30
 */
public interface SampleBusinessMpService extends IService<SampleBusiness> {

    /**
     * 查询当前登录人在 testrange 槽位下可见的全部数据
     *
     * @return 可见数据列表
     */
    List<SampleBusiness> listAuthRange();

    /**
     * 分页查询当前登录人在 testrange 槽位下可见的数据
     *
     * @param pageReq 分页参数
     * @return 可见数据分页
     */
    PageResult<SampleBusiness> pageAuthRange(PageReq pageReq);
}
