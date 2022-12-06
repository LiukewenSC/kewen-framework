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
 * 错误日志记录表
 * </p>
 *
 * @author kewen
 * @since 2022-12-05
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("sys_log")
@ApiModel(value = "错误日志记录表实体类", description = "SysLog实体对象")
public class SysLog extends Model<SysLog> {

    private static final long serialVersionUID = 1L;

    //@ApiModelProperty("id")
    @ApiModelProperty(name = "id", value = "id")
      @TableId(value = "id", type = IdType.AUTO)
    private String id;

    //@ApiModelProperty("类名称")
    @ApiModelProperty(name = "className", value = "类名称")
    @TableField("class_name")
    private String className;

    //@ApiModelProperty("方法名称")
    @ApiModelProperty(name = "methodName", value = "方法名称")
    @TableField("method_name")
    private String methodName;

    //@ApiModelProperty("异常类型")
    @ApiModelProperty(name = "exceptionName", value = "异常类型")
    @TableField("exception_name")
    private String exceptionName;

    //@ApiModelProperty("错误信息")
    @ApiModelProperty(name = "errMsg", value = "错误信息")
    @TableField("err_msg")
    private String errMsg;

    //@ApiModelProperty("异常堆栈信息")
    @ApiModelProperty(name = "stackTrace", value = "异常堆栈信息")
    @TableField("stack_trace")
    private String stackTrace;

    //@ApiModelProperty("创建时间戳")
    @ApiModelProperty(name = "createTimestamp", value = "创建时间戳")
    @TableField("create_timestamp")
    private LocalDateTime createTimestamp;

    //@ApiModelProperty("更新时间戳")
    @ApiModelProperty(name = "updateTimestamp", value = "更新时间戳")
    @TableField("update_timestamp")
    private LocalDateTime updateTimestamp;


    @Override
    public Serializable pkVal() {
        return this.id;
    }

}
