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
 * 用户角色组关联表
 * </p>
 *
 * @author kewen
 * @since 2022-12-05
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("sys_user_group")
@ApiModel(value = "用户角色组关联表实体类", description = "SysUserGroup实体对象")
public class SysUserGroup extends Model<SysUserGroup> {

    private static final long serialVersionUID = 1L;

    //@ApiModelProperty("主键id")
    @ApiModelProperty(name = "id", value = "主键id")
      @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    //@ApiModelProperty("用户id")
    @ApiModelProperty(name = "userId", value = "用户id")
    @TableField("user_id")
    private Long userId;

    //@ApiModelProperty("角色组id")
    @ApiModelProperty(name = "groupId", value = "角色组id")
    @TableField("group_id")
    private Long groupId;

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
