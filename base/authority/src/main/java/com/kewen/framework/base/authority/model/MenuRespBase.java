package com.kewen.framework.base.authority.model;

import com.kewen.framework.base.authority.enums.MenuAuthType;
import com.kewen.framework.base.authority.enums.MenuType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @descrpition 菜单返回结构
 * @author kewen
 * @since 2022-12-01 10:42
 */
@Data
public class MenuRespBase {
    /**
     * 主键id
     */
    protected Integer id;

    /**
     * 菜单名
     */
    protected String name;

    /**
     * 父id
     */
    protected Integer parentId;

    /**
     * 链接或路径
     */
    protected String url;

    /**
     * 图片地址
     */
    protected String image;

    /**
     * 类型： 1-菜单 2-按钮 3-外部链接
     */
    protected MenuType type;
    /**
     * 权限类型 1-基于父菜单权限 2-基于本身权限
     */
    protected MenuAuthType authType;

    /**
     * 描述
     */
    protected String description;

    /**
     * 创建时间
     */
    protected LocalDateTime createTime;

    /**
     * 修改时间
     */
    protected LocalDateTime updateTime;

}

