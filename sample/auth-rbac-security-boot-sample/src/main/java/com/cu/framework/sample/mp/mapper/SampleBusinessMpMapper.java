package com.cu.framework.sample.mp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cu.framework.auth.core.AuthDataRange;
import com.cu.framework.sample.mp.entity.SampleBusiness;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 示例业务数据 Mapper。
 * <p>
 * {@link AuthDataRange} 只标在本接口自己声明的查询方法上：注解标在哪个方法，权限条件就只注入那一条语句。
 * MyBatis-Plus 从 BaseMapper 继承来的方法没有挂载点，不受数据范围管辖。
 *
 * @author kewen
 * @since 2026-10-30
 */
@Mapper
public interface SampleBusinessMpMapper extends BaseMapper<SampleBusiness> {

    /**
     * 查询当前登录人在 testrange 槽位下可见的全部业务数据
     *
     * @return 可见的业务数据列表
     */
    @AuthDataRange(businessFunction = "testrange")
    @Select("select * from security_sample_business order by id")
    List<SampleBusiness> selectAuthRangeList();

    /**
     * 分页查询当前登录人在 testrange 槽位下可见的业务数据
     *
     * @param size   页面大小
     * @param offset 从 0 开始的偏移量
     * @return 可见的业务数据列表
     */
    @AuthDataRange(businessFunction = "testrange")
    @Select("select * from security_sample_business order by id limit #{size} offset #{offset}")
    List<SampleBusiness> selectAuthRangePage(@Param("size") Integer size, @Param("offset") Integer offset);

    /**
     * 统计当前登录人在 testrange 槽位下可见的业务数据总数
     *
     * @return 可见的业务数据总数
     */
    @AuthDataRange(businessFunction = "testrange")
    @Select("select count(*) from security_sample_business")
    long selectAuthRangeCount();
}
