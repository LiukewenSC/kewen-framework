package com.kewen.framework.boot.authority.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.kewen.framework.boot.authority.enums.MenuAuthType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 菜单表
 * </p>
 *
 * @author kewen
 * @since 2022-12-05
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("sys_menu")
@ApiModel(value = "菜单表实体类", description = "SysMenu实体对象")
public class SysMenu extends Model<SysMenu> {

    private static final long serialVersionUID = 1L;

    //@ApiModelProperty("主键id")
    @ApiModelProperty(name = "id", value = "主键id")
      @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    //@ApiModelProperty("菜单名")
    @ApiModelProperty(name = "name", value = "菜单名")
    @TableField("name")
    private String name;

    //@ApiModelProperty("父id")
    @ApiModelProperty(name = "parentId", value = "父id")
    @TableField("parent_id")
    private Long parentId;

    //@ApiModelProperty("链接或路径")
    @ApiModelProperty(name = "url", value = "链接或路径")
    @TableField("url")
    private String url;

    //@ApiModelProperty("图片地址")
    @ApiModelProperty(name = "image", value = "图片地址")
    @TableField("image")
    private String image;

    //@ApiModelProperty("类型： 1-菜单 2-按钮 3-外部链接")
    @ApiModelProperty(name = "type", value = "类型： 1-菜单 2-按钮 3-外部链接")
    @TableField("type")
    private Integer type;

    //@ApiModelProperty("权限类型 1-基于父菜单权限 2-基于本身权限")
    @ApiModelProperty(name = "authType", value = "权限类型 1-基于父菜单权限 2-基于本身权限")
    @TableField("auth_type")
    private MenuAuthType authType;

    //@ApiModelProperty("描述")
    @ApiModelProperty(name = "description", value = "描述")
    @TableField("description")
    private String description;

    //@ApiModelProperty("创建时间")
    @ApiModelProperty(name = "createTime", value = "创建时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    //@ApiModelProperty("修改时间")
    @ApiModelProperty(name = "updateTime", value = "修改时间")
    @TableField("update_time")
    private LocalDateTime updateTime;


    @Override
    public Serializable pkVal() {
        return this.id;
    }

}
