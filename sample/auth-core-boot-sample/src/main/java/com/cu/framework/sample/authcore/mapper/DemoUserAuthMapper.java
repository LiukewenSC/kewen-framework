package com.cu.framework.sample.authcore.mapper;

import com.cu.framework.auth.core.entity.BaseAuth;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 示例用户权限表 demo_user_auth 的读取，是 AuthCurrentUserContextHolder 的数据来源。
 * <p>
 * 这条查询<b>没有</b>声明数据范围，因此它正好充当"同一调用链上的其他语句不受影响"的观察对象：
 * 每次装载权限都会执行它，且不论当前用户有没有权限，它都按业务条件原样返回
 *
 * @author kewen
 * @since 2026-10-20
 */
public interface DemoUserAuthMapper {

    /**
     * 按用户名取出其权限串
     *
     * @param username 用户名
     * @return 权限串集合，用户不存在时为空集合
     */
    List<BaseAuth> listByUsername(@Param("username") String username);
}
