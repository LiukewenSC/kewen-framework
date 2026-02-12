/*
package com.kewen.framework.auth.sample.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kewen.framework.auth.core.AuthDataAdaptor;
import com.kewen.framework.auth.core.AuthMenu;
import com.kewen.framework.auth.rabc.model.RabcIdReq;
import com.kewen.framework.auth.rabc.model.RabcPageConverter;
import com.kewen.framework.auth.rabc.model.RabcPageReq;
import com.kewen.framework.auth.rabc.model.RabcPageResult;
import com.kewen.framework.auth.sample.model.MeetingRoomAddReq;
import com.kewen.framework.auth.sample.model.MeetingRoomResult;
import com.kewen.framework.auth.sample.model.MeetingRoomUpdatePrimaryAuth;
import com.kewen.framework.basic.model.Result;;
import com.kewen.framework.auth.sample.mp.entity.MeetingRoom;
import com.kewen.framework.auth.sample.mp.service.MeetingRoomMpService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

*/
/**
 * 会议室主要信息相关
 *
 * @author kewen
 * @since 2024-08-13
 *//*

@RestController
@RequestMapping("/meetingRoomPrimary")
public class MeetingRoomPrimaryController {
    @Autowired
    MeetingRoomMpService meetingRoomService;

    @Autowired
    AuthDataAdaptor authDataAdaptor;

    private static final String BUSINESS_FUNCTION = MeetingRoomConstant.BUSINESS_FUNCTION;
    private static final String OPERATE_EDIT_INFO = MeetingRoomConstant.OPERATE_EDIT_INFO;

    */
/**
     * 创建会议室,第二种写法，不依赖于注解
     *
     * @param entity
     * @return
     *//*

    @PostMapping("/add2")
    @AuthMenu(name = "创建会议室")
    @Transactional(rollbackFor = Exception.class)
    public Result add2(@RequestBody MeetingRoomAddReq entity) {
        meetingRoomService.save(entity);
        authDataAdaptor.editDataAuths(BUSINESS_FUNCTION, entity.getDataId(), OPERATE_EDIT_INFO, entity.getAuthObject());
        return Result.success(entity);
    }

    */
/**
     * 删除会议室
     *
     * @param req
     * @return
     *//*

    @PostMapping(value = "/deleteById")
    @AuthMenu(name = "删除会议室")
    @Transactional(rollbackFor = Exception.class)
    public Result deleteById(@RequestBody @Validated RabcIdReq req) {
        boolean b = meetingRoomService.removeById(req.getId());
        if (b) {
            //把权限也删掉
            authDataAdaptor.deleteBusinessFunctionAuthByDataId(BUSINESS_FUNCTION, req.getId());
        }

        return Result.success();
    }

    @PostMapping("/update")
    @AuthMenu(name = "编辑会议室主要信息及主权限")
    @Transactional(rollbackFor = Exception.class)
    public Result updateMeetingRoomRoot(@RequestBody MeetingRoomUpdatePrimaryAuth req) {
        boolean b = meetingRoomService.updateById(req);
        if (b) {
            authDataAdaptor.editDataAuths(BUSINESS_FUNCTION, req.getId(), OPERATE_EDIT_INFO, req.getAuthObject());
        }
        return Result.success(req);
    }

    @GetMapping("/pageRoom")
    @AuthMenu(name = "会议室主权限分类")
    public Result pageMeetingRoom(RabcPageReq req) {
        RabcPageResult<MeetingRoomResult> rPageResult = RabcPageConverter.pageAndConvert(
                req,
                meetingRoomService,
                new RabcPageConverter.Query<MeetingRoom>()
                        .setWrapper(new LambdaQueryWrapper<MeetingRoom>()
                                .like(StringUtils.isNotBlank(req.getSearch()), MeetingRoom::getName, req.getSearch())),
                meetingRooms -> meetingRooms.stream().map(m -> MeetingRoomResult.from(m, roomResult -> {
                    authDataAdaptor.fillDataAuths(BUSINESS_FUNCTION, roomResult.getId(), OPERATE_EDIT_INFO, roomResult.getAuthObject());
                })).collect(Collectors.toList())
        );
        return Result.success(rPageResult);
    }

}
*/
