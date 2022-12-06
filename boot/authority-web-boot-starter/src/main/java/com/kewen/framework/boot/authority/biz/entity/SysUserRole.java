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
 * 
 * </p>
 *
 * @author kewen
 * @since 2022-12-05
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("sys_user_role")
@ApiModel(value = "实体类", description = "SysUserRole实体对象")
public class SysUserRole extends Model<SysUserRole> {

    private static final long serialVersionUID = 1L;

    //@ApiModelProperty("主键id")
    @ApiModelProperty(name = "id", value = "主键id")
      @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    //@ApiModelProperty("用户id")
    @ApiModelProperty(name = "userId", value = "用户id")
    @TableField("user_id")
    private Long userId;

    //@ApiModelProperty("角色id")
    @ApiModelProperty(name = "roleId", value = "角色id")
    @TableField("role_id")
    private Long roleId;

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
