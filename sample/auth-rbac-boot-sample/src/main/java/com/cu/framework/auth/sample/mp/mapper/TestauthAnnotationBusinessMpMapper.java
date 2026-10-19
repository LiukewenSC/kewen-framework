package com.cu.framework.auth.sample.mp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cu.framework.auth.core.AuthDataRange;
import com.cu.framework.auth.sample.mp.entity.TestauthAnnotationBusiness;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 * <p>
 * 数据范围声明（{@link AuthDataRange}）标在本接口自己声明的查询方法上，
 * MyBatis-Plus 从 BaseMapper 继承的方法没有挂载点，因此按权限过滤的查询在此处显式写出
 *
 * @author kewen
 * @since 2023-12-26
 */
@Mapper
public interface TestauthAnnotationBusinessMpMapper extends BaseMapper<TestauthAnnotationBusiness> {

    /**
     * 查询当前登录人在 testrange 槽位下可见的全部业务数据
     * @return 可见的业务数据列表
     */
    @AuthDataRange(businessFunction = "testrange")
    @Select("select * from testauth_annotation_business order by id")
    List<TestauthAnnotationBusiness> selectAuthRangeList();

    /**
     * 分页查询当前登录人在 testrange 槽位下可见的业务数据
     * @param size 页面大小
     * @param offset 从0开始的偏移量
     * @return 可见的业务数据列表
     */
    @AuthDataRange(businessFunction = "testrange")
    @Select("select * from testauth_annotation_business order by id limit #{size} offset #{offset}")
    List<TestauthAnnotationBusiness> selectAuthRangePage(@Param("size") Integer size, @Param("offset") Integer offset);

    /**
     * 统计当前登录人在 testrange 槽位下可见的业务数据总数
     * @return 可见的业务数据总数
     */
    @AuthDataRange(businessFunction = "testrange")
    @Select("select count(*) from testauth_annotation_business")
    long selectAuthRangeCount();

}
