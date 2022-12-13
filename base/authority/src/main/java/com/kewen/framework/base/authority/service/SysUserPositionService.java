package com.kewen.framework.base.authority.service;

import com.kewen.framework.base.authority.entity.SysUserPosition;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kewen.framework.base.common.model.Position;


import java.util.List;


/**
 * <p>
 * 用户岗位关联表 服务类
 * </p>
 *
 * @author kewen
 * @since 2022-12-07
 */
public interface SysUserPositionService extends IService<SysUserPosition> {

    List<Position> listUserPosition(Long userId);
}
