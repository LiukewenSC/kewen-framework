package com.cu.framework.auth.rbac.composite.mapper;

import com.cu.framework.auth.rbac.composite.model.UserAuthInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author kewen
 * @descrpition
 * @since 2023-04-07
 */
@Mapper
public interface SysUserUnionCompositeMapper {

    /**
     * 查找用户的角色、机构等
     * @param id
     * @return
     */
    UserAuthInfo getUserAuthInfo(@Param("id") Long id);
}
