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
 * 岗位表
 * </p>
 *
 * @author kewen
 * @since 2022-12-05
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("sys_position")
@ApiModel(value = "岗位表实体类", description = "SysPosition实体对象")
public class SysPosition extends Model<SysPosition> {

    private static final long serialVersionUID = 1L;

    //@ApiModelProperty("岗位id")
    @ApiModelProperty(name = "id", value = "岗位id")
      @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    //@ApiModelProperty("岗位名")
    @ApiModelProperty(name = "name", value = "岗位名")
    @TableField("name")
    private String name;

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
