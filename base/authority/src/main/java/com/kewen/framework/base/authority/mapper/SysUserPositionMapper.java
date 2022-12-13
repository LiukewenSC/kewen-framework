package com.kewen.framework.base.authority.mapper;

import com.kewen.framework.base.authority.entity.SysUserPosition;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kewen.framework.base.common.model.Position;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 用户岗位关联表 Mapper 接口
 * </p>
 *
 * @author kewen
 * @since 2022-12-07
 */
public interface SysUserPositionMapper extends BaseMapper<SysUserPosition> {
    /**
     * 查询用户岗位列表
     * @param userId
     * @return
     */
    List<Position> listUserPosition(@Param("userId") Long userId);
}
