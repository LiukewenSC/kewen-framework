package com.cu.framework.auth.sample.mp.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.cu.framework.auth.sample.mp.entity.MeetingRoom;
import com.cu.framework.basic.model.PageReq;
import com.cu.framework.basic.model.PageResult;


/**
 * <p>
 *  服务类
 * </p>
 *
 * @author kewen
 * @since 2024-08-06
 */
public interface MeetingRoomMpService extends IService<MeetingRoom> {

    /**
     * 分页查询当前登录人可编辑的会议室
     * @param pageReq 分页参数
     * @return 可编辑的会议室分页
     */
    PageResult<MeetingRoom> pageEditAble(PageReq pageReq);

    /**
     * 分页查询当前登录人可预约的会议室
     * @param pageReq 分页参数
     * @return 可预约的会议室分页
     */
    PageResult<MeetingRoom> pageApproveAble(PageReq pageReq);

}
