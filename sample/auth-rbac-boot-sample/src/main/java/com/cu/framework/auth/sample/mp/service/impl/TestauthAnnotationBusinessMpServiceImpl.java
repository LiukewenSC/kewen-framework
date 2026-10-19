package com.cu.framework.auth.sample.mp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cu.framework.auth.sample.mp.entity.TestauthAnnotationBusiness;
import com.cu.framework.auth.sample.mp.mapper.TestauthAnnotationBusinessMpMapper;
import com.cu.framework.auth.sample.mp.service.TestauthAnnotationBusinessMpService;
import com.cu.framework.basic.model.PageReq;
import com.cu.framework.basic.model.PageResult;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 * <p>
 * 按权限过滤的查询走 Mapper 上手写的语句：MyBatis-Plus 从 BaseMapper 继承的 list、page 没有数据范围声明的挂载点
 *
 * @author kewen
 * @since 2023-12-26
 */
@Service
public class TestauthAnnotationBusinessMpServiceImpl extends ServiceImpl<TestauthAnnotationBusinessMpMapper, TestauthAnnotationBusiness> implements TestauthAnnotationBusinessMpService {

    @Override
    public List<TestauthAnnotationBusiness> listAuthRange() {
        return getBaseMapper().selectAuthRangeList();
    }

    @Override
    public PageResult<TestauthAnnotationBusiness> pageAuthRange(PageReq pageReq) {
        TestauthAnnotationBusinessMpMapper mapper = getBaseMapper();
        List<TestauthAnnotationBusiness> records = mapper.selectAuthRangePage(pageReq.getSize(), pageReq.getOffset());
        long total = mapper.selectAuthRangeCount();
        return PageResult.of(pageReq.getPage(), pageReq.getSize(), (int) total, records);
    }

}
