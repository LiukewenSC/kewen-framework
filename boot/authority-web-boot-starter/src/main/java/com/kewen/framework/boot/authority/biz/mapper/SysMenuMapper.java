package com.kewen.framework.boot.authority.biz.mapper;

import com.kewen.framework.boot.authority.biz.entity.SysMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;

/**
 * <p>
 * 菜单表 Mapper 接口
 * </p>
 *
 * @author kewen
 * @since 2022-12-05
 */
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    Integer hasAuth(@Param("authorities") Collection<String> authorities, @Param("url") String url);
}
