package com.kewen.framework.auth.sys.composite.mapper;

import com.kewen.framework.auth.sys.model.DeptPrimary;
import com.kewen.framework.auth.sys.model.Position;
import com.kewen.framework.auth.sys.model.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * @author kewen
 * @descrpition
 * @since 2023-04-07
 */
@Mapper
public interface SysUserCompositeMapper {
    List<DeptPrimary> listUserDept(Long userId);

    List<Role> listUserRole(@Param("userId") Long userId);

    /**
     * 查询用户岗位列表
     * @param userId
     * @return
     */
    List<Position> listUserPosition(@Param("userId") Long userId);


    Integer hasAuth(@Param("auths") Collection<String> auths, @Param("module") String module, @Param("operate") String operate, @Param("businessId") Long businessId);


}
