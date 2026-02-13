package com.kewen.framework.auth.sample.controller;

import com.kewen.framework.auth.core.AuthDataOperation;
import com.kewen.framework.auth.core.AuthDataRange;
import com.kewen.framework.auth.core.AuthMenu;
import com.kewen.framework.auth.rabc.model.RabcPageConverter;
import com.kewen.framework.auth.rabc.model.RabcPageReq;
import com.kewen.framework.auth.rabc.model.RabcPageResult;
import com.kewen.framework.auth.sample.model.MeetingRoomApproveReq;
import com.kewen.framework.auth.sample.mp.entity.MeetingRoom;
import com.kewen.framework.auth.sample.mp.service.MeetingRoomMpService;
import com.kewen.framework.basic.model.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


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
     * @param RabcPageReq
     * @return
     */
    @GetMapping(value = "/page")
    @AuthDataRange(businessFunction = BUSINESS_FUNCTION,operate = OPERATE_APPROVE)
    public Result<RabcPageResult<MeetingRoom>> pageapproves(@Validated RabcPageReq RabcPageReq) {
        RabcPageResult<MeetingRoom> meetingRoomPageResult = RabcPageConverter.pageAndConvert(RabcPageReq, meetingRoomService);
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
