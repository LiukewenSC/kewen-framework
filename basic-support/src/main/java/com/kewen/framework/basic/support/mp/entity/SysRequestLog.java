package com.kewen.framework.basic.support.mp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 请求日志记录器
 * </p>
 *
 * @author kewen
 * @since 2023-04-26
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("sys_request_log")
public class SysRequestLog extends Model<SysRequestLog> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户姓名
     */
    @TableField("user_name")
    private String userName;

    /**
     * API路径
     */
    @TableField("api_path")
    private String api_path;

    /**
     * 请求方式
     */
    @TableField("method")
    private String method;

    /**
     * 请求持续时间
     */
    @TableField("millisecond")
    private Integer millisecond;

    /**
     * IP地址
     */
    @TableField("ip_address")
    private String ipAddress;

    /**
     * IP地址描述
     */
    @TableField("ip_comment")
    private String ipComment;

    /**
     * 请求param参数
     */
    @TableField("params")
    private String params;

    /**
     * 请求body参数
     */
    @TableField("body")
    private String body;

    /**
     * 状态成功失败
     */
    @TableField("status")
    private Integer status;
    /**
     * 请求跟踪号
     */
    @TableField("trace_id")
    private String traceId;

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
