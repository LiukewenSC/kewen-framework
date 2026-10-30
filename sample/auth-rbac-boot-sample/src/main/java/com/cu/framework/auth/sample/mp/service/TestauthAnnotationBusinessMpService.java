package com.cu.framework.auth.sample.mp.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.cu.framework.auth.sample.mp.entity.TestauthAnnotationBusiness;
import com.cu.framework.basic.model.PageReq;
import com.cu.framework.basic.model.PageResult;

import java.util.List;


/**
 * <p>
 *  服务类
 * </p>
 *
 * @author kewen
 * @since 2023-12-26
 */
public interface TestauthAnnotationBusinessMpService extends IService<TestauthAnnotationBusiness> {

    /**
     * 查询当前登录人在 testrange 槽位下可见的全部数据
     * @return 可见数据列表
     */
    List<TestauthAnnotationBusiness> listAuthRange();

    /**
     * 分页查询当前登录人在 testrange 槽位下可见的数据
     * @param pageReq 分页参数
     * @return 可见数据分页
     */
    PageResult<TestauthAnnotationBusiness> pageAuthRange(PageReq pageReq);

}
