package com.cu.framework.auth.sample.controller;

import com.cu.framework.auth.core.AuthDataAdaptor;
import com.cu.framework.auth.core.AuthDataOperation;
import com.cu.framework.auth.core.AuthMenu;
import com.cu.framework.auth.core.AuthMenuDoc;
import com.cu.framework.auth.sample.mp.entity.MeetingRoom;
import com.cu.framework.auth.sample.mp.service.MeetingRoomMpService;
import com.cu.framework.auth.sample.model.MeetingRoomResult;
import com.cu.framework.auth.sample.model.MeetingRoomUpdateReq;
import com.cu.framework.basic.model.PageReq;
import com.cu.framework.basic.model.PageResult;
import com.cu.framework.basic.model.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;


/**
 * 会议室基本信息管理
 *
 * @author kewen
 * @since 2024-08-06
 */
@RestController
@RequestMapping("/meetingRoom")
@AuthMenu
@AuthMenuDoc(name = "会议室信息管理")
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
     * 编辑会议室方式2：
     * 编辑权限由 @AuthDataOperation 校验，预约权限的写入直接调用 AuthDataAdaptor
     *
     * @param entity
     * @return
     */
    @PostMapping("/updateById")
    @Transactional(rollbackFor = Exception.class)
    @AuthDataOperation(businessFunction = BUSINESS_FUNCTION, operate = OPERATE_EDIT_INFO)
    public Result updateByIdMethod(@RequestBody MeetingRoomUpdateReq entity) {
        meetingRoomService.updateById(entity);
        authDataAdaptor.editDataAuths(BUSINESS_FUNCTION, entity.getId(), OPERATE_APPROVE, entity.getAuthObject());
        return Result.success(entity);
    }

    /**
     * 查询可编辑列表。
     * 数据范围声明在 MeetingRoomMpMapper 的查询方法上，这里只做结果转换与权限填充
     *
     * @param pageReq
     * @return
     */
    @GetMapping(value = "/page")
    public Result<PageResult<MeetingRoomResult>> pageQuery(@Validated PageReq pageReq) {
        PageResult<MeetingRoom> page = meetingRoomService.pageEditAble(pageReq);
        List<MeetingRoomResult> data = page.getData().stream().map(m -> {
            MeetingRoomResult result = MeetingRoomResult.from(m);
            authDataAdaptor.fillDataAuths(BUSINESS_FUNCTION, m.getId(), OPERATE_APPROVE, result.getAuthObject());
            return result;
        }).collect(Collectors.toList());
        PageResult<MeetingRoomResult> meetingRoomPageResult =
                PageResult.of(page.getPage(), page.getSize(), page.getTotal(), data);
        return Result.success(meetingRoomPageResult);
    }

}
