package com.cu.framework.sample.controller;

import com.cu.framework.auth.core.AuthDataOperation;
import com.cu.framework.auth.core.AuthMenu;
import com.cu.framework.auth.core.AuthMenuDoc;
import com.cu.framework.sample.mp.entity.MeetingRoom;
import com.cu.framework.sample.mp.service.MeetingRoomMpService;
import com.cu.framework.sample.model.MeetingRoomApproveReq;
import com.cu.framework.basic.model.PageReq;
import com.cu.framework.basic.model.PageResult;
import com.cu.framework.basic.model.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 *  会议室基本信息管理
 *
 * @author kewen
 * @since 2024-08-06
 */
@RestController
@RequestMapping("/meetingRoomApprove")
@AuthMenu
@AuthMenuDoc(name = "会议室预约")
public class MeetingRoomApproveController {

    private static final Logger log = LoggerFactory.getLogger(MeetingRoomApproveController.class);

    /**
     * 业务功能
     */
    private static final String BUSINESS_FUNCTION=MeetingRoomConstant.BUSINESS_FUNCTION;

    /**
     * 会议室预约操作
     */
    private static final String OPERATE_APPROVE = MeetingRoomConstant.OPERATE_APPROVE;


    @Autowired
    MeetingRoomMpService meetingRoomService;

    /**
     * 查询可预约列表
     * @param pageReq
     * @return
     */
    @GetMapping(value = "/page")
    public Result<PageResult<MeetingRoom>> pageapproves(@Validated PageReq pageReq) {
        //数据范围声明在 MeetingRoomMpMapper 的可预约查询方法上，这里只取分页结果
        PageResult<MeetingRoom> meetingRoomPageResult = meetingRoomService.pageApproveAble(pageReq);
        return Result.success(meetingRoomPageResult);
    }

    /**
     * 预约会议室
     * @param req 会议室ID，其实还有其他的入参，比如时间段等，这里暂时不管
     * @return
     */
    @PostMapping(value = "/approve")
    @AuthDataOperation(businessFunction = BUSINESS_FUNCTION,operate = OPERATE_APPROVE)
    public Result approveMeetingRoom(@RequestBody @Validated MeetingRoomApproveReq req) {
        String time = req.getTime();
        //预约会议室的逻辑
        log.info("预约成功{}", time);
        return Result.success(time);
    }

}
