package com.cu.framework.sample.authcore.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 示例菜单授权表 demo_menu_auth 的读取，是 AuthMenuHandler 的数据来源
 *
 * @author kewen
 * @since 2026-10-20
 */
public interface DemoMenuAuthMapper {

    /**
     * 取出被授予某个应用内路径的权限串
     *
     * @param path 应用内路径，不含 contextPath
     * @return 权限串列表，无人被授予时为空列表
     */
    List<String> listAuthsByPath(@Param("path") String path);

    /**
     * 判断某路径是否登记过。未登记的路径视为"没有授予任何人"，而不是"谁都能进"
     *
     * @param path 应用内路径
     * @return 登记行数
     */
    int countByPath(@Param("path") String path);
}
