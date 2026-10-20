package com.cu.framework.sample.authcore.entity;

import lombok.Data;

/**
 * 示例业务表 demo_menu_api 的行，也是示例自己实现的 MenuApiService 的存储载体
 *
 * @author kewen
 * @since 2026-10-20
 */
@Data
public class MenuApiPO {

    /** 主键，由示例用雪花算法生成 */
    private Long id;
    /** 请求路径，菜单行的唯一标识 */
    private String path;
    /** 菜单显示名称 */
    private String name;
    /** 父节点ID，根为 0 */
    private Long parentId;
    /** 备注，供使用者手工改写以验证启动过程不覆写已有行 */
    private String remark;
}
