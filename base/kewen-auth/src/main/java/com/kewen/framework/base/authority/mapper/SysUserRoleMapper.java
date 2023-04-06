package com.kewen.framework.base.authority.mapper;

import com.kewen.framework.base.authority.entity.SysUserRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kewen.framework.base.common.model.Role;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author kewen
 * @since 2022-12-07
 */
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    List<Role> listUserRole(@Param("userId") Integer userId);
}
