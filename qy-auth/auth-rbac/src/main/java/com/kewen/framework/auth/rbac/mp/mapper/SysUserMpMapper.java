package com.kewen.framework.auth.rbac.mp.mapper;

import com.kewen.framework.auth.rbac.mp.entity.SysUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 用户表 Mapper 接口
 * </p>
 *
 * @author kewen
 * @since 2024-07-29
 */
@Mapper
public interface SysUserMpMapper extends BaseMapper<SysUser> {

}
