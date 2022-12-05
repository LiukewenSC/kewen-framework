package com.kewen.framework.boot.authority.biz.mapper.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kewen.framework.boot.authority.enums.MenuAuthType;
import com.kewen.framework.boot.authority.enums.MenuType;
import lombok.Data;

import java.util.Date;

/**
 * 菜单表
 */
@Data
@TableName(value = "sys_menu")
public class SysMenu {
    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 菜单名
     */
    @TableField(value = "name")
    private String name;

    /**
     * 父id
     */
    @TableField(value = "parent_id")
    private Integer parentId;

    /**
     * 链接或路径
     */
    @TableField(value = "url")
    private String url;

    /**
     * 图片地址
     */
    @TableField(value = "image")
    private String image;

    /**
     * 类型： 1-菜单 2-按钮 3-外部链接
     */
    @TableField(value = "type")
    private MenuType type;

    /**
     * 权限类型 1-基于父菜单权限 2-基于本身权限
     */
    @TableField(value = "auth_type")
    private MenuAuthType authType;

    /**
     * 描述
     */
    @TableField(value = "description")
    private String description;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 修改时间
     */
    @TableField(value = "update_time")
    private Date updateTime;
}