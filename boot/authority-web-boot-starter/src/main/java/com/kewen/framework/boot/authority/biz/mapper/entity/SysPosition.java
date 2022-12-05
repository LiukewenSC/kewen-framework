package com.kewen.framework.boot.authority.biz.mapper.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
    * 岗位表
    */
@Data
@TableName(value = "sys_position")
public class SysPosition {
    /**
     * 岗位id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 岗位名
     */
    @TableField(value = "name")
    private String name;

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