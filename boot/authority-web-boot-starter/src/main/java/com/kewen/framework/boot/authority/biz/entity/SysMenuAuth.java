package com.kewen.framework.boot.authority.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 菜单权限表
 * </p>
 *
 * @author kewen
 * @since 2022-12-05
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("sys_menu_auth")
@ApiModel(value = "菜单权限表实体类", description = "SysMenuAuth实体对象")
public class SysMenuAuth extends Model<SysMenuAuth> {

    private static final long serialVersionUID = 1L;

    //@ApiModelProperty("主键id")
    @ApiModelProperty(name = "id", value = "主键id")
      @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    //@ApiModelProperty("菜单id")
    @ApiModelProperty(name = "menuId", value = "菜单id")
    @TableField("menu_id")
    private Long menuId;

    //@ApiModelProperty("权限字符串")
    @ApiModelProperty(name = "authority", value = "权限字符串")
    @TableField("authority")
    private String authority;

    //@ApiModelProperty("权限描述")
    @ApiModelProperty(name = "description", value = "权限描述")
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
