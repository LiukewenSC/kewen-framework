package com.cu.framework.sample.authcore.mapper;

import com.cu.framework.sample.authcore.entity.MenuApiPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 示例菜单API表 demo_menu_api 的读写，是 MenuApiService 的存储通道
 *
 * @author kewen
 * @since 2026-10-20
 */
public interface DemoMenuApiMapper {

    /**
     * 全部菜单行，供启动时的菜单生成过程判断哪些路径已存在
     *
     * @return 菜单行列表
     */
    List<MenuApiPO> listAll();

    /**
     * 批量插入新登记的菜单行
     *
     * @param list 待插入行，ID 与 parentId 已由上层填好
     * @return 插入条数
     */
    int insertBatch(@Param("list") List<MenuApiPO> list);

    /**
     * 人工改写一条已有菜单行。
     * 框架没有提供任何更新端点（core 只交 list/saveBatch），
     * 而"启动过程不覆写已有行"这条规格必须先把行改起来才能验证，所以由示例开这一个后门
     *
     * @param path   要改的菜单行路径
     * @param name   人工设定的名称
     * @param remark 人工设定的备注
     * @return 更新条数
     */
    int touchByPath(@Param("path") String path, @Param("name") String name, @Param("remark") String remark);
}
