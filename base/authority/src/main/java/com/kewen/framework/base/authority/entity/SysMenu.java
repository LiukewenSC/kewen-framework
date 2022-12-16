package com.kewen.framework.base.authority.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.kewen.framework.base.authority.enums.MenuAuthType;
import com.kewen.framework.base.authority.enums.MenuType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 菜单表
 * </p>
 *
 * @author kewen
 * @since 2022-12-07
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("sys_menu")
public class SysMenu extends Model<SysMenu> {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
      @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 菜单名
     */
    @TableField("name")
    private String name;

    /**
     * 父id
     */
    @TableField("parent_id")
    private Integer parentId;

    /**
     * 链接或路径
     */
    @TableField("url")
    private String url;

    /**
     * 图片地址
     */
    @TableField("image")
    private String image;

    /**
     * 类型： 1-菜单 2-按钮 3-外部链接
     */
    @TableField("type")
    private MenuType type;

    /**
     * 权限类型 1-基于父菜单权限 2-基于本身权限
     */
    @TableField("auth_type")
    private MenuAuthType authType;

    /**
     * 描述
     */
    @TableField("description")
    private String description;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;


    @Override
    public Serializable pkVal() {
        return this.id;
    }

}
