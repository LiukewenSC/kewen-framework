package com.cu.framework.sample.mp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cu.framework.basic.model.PageReq;
import com.cu.framework.basic.model.PageResult;
import com.cu.framework.sample.mp.entity.SampleBusiness;
import com.cu.framework.sample.mp.mapper.SampleBusinessMpMapper;
import com.cu.framework.sample.mp.service.SampleBusinessMpService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 示例业务数据服务实现。
 * <p>
 * 按权限过滤的分页走 Mapper 上手写的列表与计数语句，不复用 MyBatis-Plus 从 BaseMapper 继承的方法：
 * 那些方法没有 {@code @AuthDataRange} 的挂载点。
 *
 * @author kewen
 * @since 2026-10-30
 */
@Service
public class SampleBusinessMpServiceImpl extends ServiceImpl<SampleBusinessMpMapper, SampleBusiness>
        implements SampleBusinessMpService {

    @Override
    public List<SampleBusiness> listAuthRange() {
        return getBaseMapper().selectAuthRangeList();
    }

    @Override
    public PageResult<SampleBusiness> pageAuthRange(PageReq pageReq) {
        SampleBusinessMpMapper mapper = getBaseMapper();
        List<SampleBusiness> records = mapper.selectAuthRangePage(pageReq.getSize(), pageReq.getOffset());
        long total = mapper.selectAuthRangeCount();
        return PageResult.of(pageReq.getPage(), pageReq.getSize(), (int) total, records);
    }
}
