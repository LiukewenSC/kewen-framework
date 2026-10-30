package com.cu.framework.sample.mp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cu.framework.auth.core.AuthDataRange;
import com.cu.framework.sample.controller.MeetingRoomConstant;
import com.cu.framework.sample.mp.entity.MeetingRoom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 * <p>
 * 数据范围声明（{@link AuthDataRange}）只能标在本接口自己声明的查询方法上：
 * 注解标在哪个方法，权限条件就只注入那一条语句。
 * MyBatis-Plus 从 BaseMapper 继承来的 selectList、page 等方法没有挂载点，不受数据范围管辖，
 * 因此需要按权限过滤的查询都在此处显式写出来
 *
 * @author kewen
 * @since 2024-08-06
 */
@Mapper
public interface MeetingRoomMpMapper extends BaseMapper<MeetingRoom> {

    /**
     * 分页查询当前登录人可编辑的会议室
     * @param size 页面大小
     * @param offset 从0开始的偏移量
     * @return 可编辑的会议室列表
     */
    @AuthDataRange(businessFunction = MeetingRoomConstant.BUSINESS_FUNCTION,
            operate = MeetingRoomConstant.OPERATE_EDIT_INFO)
    @Select("select * from security_sample_meeting_room order by id limit #{size} offset #{offset}")
    List<MeetingRoom> selectEditAblePage(@Param("size") Integer size, @Param("offset") Integer offset);

    /**
     * 统计当前登录人可编辑的会议室总数，与 {@link #selectEditAblePage} 同一权限槽位，保证总数与行集口径一致
     * @return 可编辑的会议室总数
     */
    @AuthDataRange(businessFunction = MeetingRoomConstant.BUSINESS_FUNCTION,
            operate = MeetingRoomConstant.OPERATE_EDIT_INFO)
    @Select("select count(*) from security_sample_meeting_room")
    long selectEditAbleCount();

    /**
     * 分页查询当前登录人可预约的会议室
     * @param size 页面大小
     * @param offset 从0开始的偏移量
     * @return 可预约的会议室列表
     */
    @AuthDataRange(businessFunction = MeetingRoomConstant.BUSINESS_FUNCTION,
            operate = MeetingRoomConstant.OPERATE_APPROVE)
    @Select("select * from security_sample_meeting_room order by id limit #{size} offset #{offset}")
    List<MeetingRoom> selectApproveAblePage(@Param("size") Integer size, @Param("offset") Integer offset);

    /**
     * 统计当前登录人可预约的会议室总数，与 {@link #selectApproveAblePage} 同一权限槽位
     * @return 可预约的会议室总数
     */
    @AuthDataRange(businessFunction = MeetingRoomConstant.BUSINESS_FUNCTION,
            operate = MeetingRoomConstant.OPERATE_APPROVE)
    @Select("select count(*) from security_sample_meeting_room")
    long selectApproveAbleCount();

}
