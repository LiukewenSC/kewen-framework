package com.kewen.framework.auth.sample.controller;

import com.kewen.framework.auth.core.AuthDataAdaptor;
import com.kewen.framework.auth.core.AuthDataOperation;
import com.kewen.framework.auth.core.AuthDataRange;
import com.kewen.framework.auth.core.AuthMenu;
import com.kewen.framework.auth.rabc.model.RabcPageConverter;
import com.kewen.framework.auth.rabc.model.RabcPageReq;
import com.kewen.framework.auth.rabc.model.RabcPageResult;
import com.kewen.framework.auth.sample.model.MeetingRoomResult;
import com.kewen.framework.auth.sample.model.MeetingRoomUpdateReq;
import com.kewen.framework.basic.model.Result;;
import com.kewen.framework.auth.sample.mp.service.MeetingRoomMpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;


/**
 * 会议室基本信息管理
 *
 * @author kewen
 * @since 2024-08-06
 */
@RestController
@RequestMapping("/meetingRoom")
@AuthMenu(name = "会议室信息管理")
public class MeetingRoomController {

    private static final Logger log = LoggerFactory.getLogger(MeetingRoomController.class);


    /**
     * 业务功能
     */
    private static final String BUSINESS_FUNCTION = MeetingRoomConstant.BUSINESS_FUNCTION;
    /**
     * 会议室编辑操作
     */
    private static final String OPERATE_EDIT_INFO = MeetingRoomConstant.OPERATE_EDIT_INFO;
    /**
     * 会议室预约操作
     */
    private static final String OPERATE_APPROVE = MeetingRoomConstant.OPERATE_APPROVE;


    @Autowired
    MeetingRoomMpService meetingRoomService;

    @Autowired
    AuthDataAdaptor authDataAdaptor;

    /**
     * 编辑会议室方式2，
     * 不依赖@AuthDataAuthEdit
     *
     * @param entity
     * @return
     */
    @PostMapping("/updateById")
    @Transactional(rollbackFor = Exception.class)
    @AuthDataOperation(businessFunction = BUSINESS_FUNCTION, operate = OPERATE_EDIT_INFO)
    public Result updateByIdMethod(@RequestBody MeetingRoomUpdateReq entity) {
        meetingRoomService.updateById(entity);
        authDataAdaptor.editDataAuths(BUSINESS_FUNCTION, entity.getDataId(), OPERATE_APPROVE, entity.getAuthObject());
        return Result.success(entity);
    }

    /**
     * 查询可编辑列表
     *
     * @param RabcPageReq
     * @return
     */
    @GetMapping(value = "/page")
    @AuthDataRange(businessFunction = BUSINESS_FUNCTION, operate = OPERATE_EDIT_INFO)
    public Result<RabcPageResult<MeetingRoomResult>> pageQuery(@Validated RabcPageReq RabcPageReq) {
        RabcPageResult<MeetingRoomResult> meetingRoomPageResult = RabcPageConverter.pageAndConvert(
                RabcPageReq,
                meetingRoomService,
                null,
                meetingRooms -> meetingRooms.stream().map(m -> {
                    MeetingRoomResult result = MeetingRoomResult.from(m);
                    authDataAdaptor.fillDataAuths(BUSINESS_FUNCTION, m.getId(), OPERATE_APPROVE, result.getAuthObject());
                    return result;
                }).collect(Collectors.toList())
        );
        return Result.success(meetingRoomPageResult);
    }

}
