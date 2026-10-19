package com.cu.framework.auth.sample.mp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cu.framework.auth.sample.mp.entity.MeetingRoom;
import com.cu.framework.auth.sample.mp.mapper.MeetingRoomMpMapper;
import com.cu.framework.auth.sample.mp.service.MeetingRoomMpService;
import com.cu.framework.basic.model.PageReq;
import com.cu.framework.basic.model.PageResult;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 * <p>
 * 按权限过滤的分页走 Mapper 上手写的列表与计数语句，不复用 MybatisPageExecutor：
 * 后者的入参绑定 MyBatis-Plus 的 IService 与 Page，落到 BaseMapper 的内建方法上就没有数据范围声明的挂载点
 *
 * @author kewen
 * @since 2024-08-06
 */
@Service
public class MeetingRoomMpServiceImpl extends ServiceImpl<MeetingRoomMpMapper, MeetingRoom> implements MeetingRoomMpService {

    @Override
    public PageResult<MeetingRoom> pageEditAble(PageReq pageReq) {
        MeetingRoomMpMapper mapper = getBaseMapper();
        List<MeetingRoom> records = mapper.selectEditAblePage(pageReq.getSize(), pageReq.getOffset());
        long total = mapper.selectEditAbleCount();
        return PageResult.of(pageReq.getPage(), pageReq.getSize(), (int) total, records);
    }

    @Override
    public PageResult<MeetingRoom> pageApproveAble(PageReq pageReq) {
        MeetingRoomMpMapper mapper = getBaseMapper();
        List<MeetingRoom> records = mapper.selectApproveAblePage(pageReq.getSize(), pageReq.getOffset());
        long total = mapper.selectApproveAbleCount();
        return PageResult.of(pageReq.getPage(), pageReq.getSize(), (int) total, records);
    }

}
