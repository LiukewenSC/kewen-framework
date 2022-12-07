package com.kewen.framework.base.authority.mapper;

import com.kewen.framework.base.authority.entity.SysApplicationAuth;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;

/**
 * <p>
 * 应用权限表 Mapper 接口
 * </p>
 *
 * @author kewen
 * @since 2022-12-07
 */
public interface SysApplicationAuthMapper extends BaseMapper<SysApplicationAuth> {

    Integer hasAuth(@Param("auths") Collection<String> auths, @Param("module") String module, @Param("operate") String operate, @Param("businessId") Long businessId);
}
