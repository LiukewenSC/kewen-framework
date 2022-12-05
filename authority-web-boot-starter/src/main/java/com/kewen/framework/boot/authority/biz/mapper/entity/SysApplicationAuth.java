package com.kewen.framework.boot.authority.biz.mapper.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 应用权限表
 */
@Data
@TableName(value = "sys_application_auth")
public class SysApplicationAuth {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 模块
     */
    @TableField(value = "module")
    private String module;

    /**
     * 业务ID 应用中业务的主键ID
     */
    @TableField(value = "business_id")
    private Integer businessId;

    /**
     * 操作类型 unified modify delete 等,应用可以自定义操作名称
     */
    @TableField(value = "operate")
    private String operate;

    /**
     * 权限字符串
     */
    @TableField(value = "authority")
    private String authority;

    /**
     * 权限描述
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