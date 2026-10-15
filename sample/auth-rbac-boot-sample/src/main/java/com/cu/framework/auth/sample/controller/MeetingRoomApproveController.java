package com.cu.framework.auth.sample.controller;

import com.cu.framework.auth.core.AuthDataOperation;
import com.cu.framework.auth.core.AuthDataRange;
import com.cu.framework.auth.core.AuthMenu;
import com.cu.framework.auth.rbac.model.RbacPageConverter;
import com.cu.framework.auth.rbac.model.RbacPageReq;
import com.cu.framework.auth.rbac.model.RbacPageResult;
import com.cu.framework.auth.sample.model.MeetingRoomApproveReq;
import com.cu.framework.auth.sample.mp.entity.MeetingRoom;
import com.cu.framework.auth.sample.mp.service.MeetingRoomMpService;
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
@AuthMenu(name = "会议室预约")
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
     * @param RbacPageReq
     * @return
     */
    @GetMapping(value = "/page")
    @AuthDataRange(businessFunction = BUSINESS_FUNCTION,operate = OPERATE_APPROVE)
    public Result<RbacPageResult<MeetingRoom>> pageapproves(@Validated RbacPageReq RbacPageReq) {
        RbacPageResult<MeetingRoom> meetingRoomPageResult = RbacPageConverter.pageAndConvert(RbacPageReq, meetingRoomService);
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
