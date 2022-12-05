package com.kewen.framework.boot.authority.biz.mapper.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
    * 部门表
    */
@Data
@TableName(value = "sys_dept")
public class SysDept {
    /**
     * 部门id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 部门名
     */
    @TableField(value = "name")
    private String name;

    /**
     * 部门，如果部门为0 则代表根部门
     */
    @TableField(value = "parent_id")
    private Integer parentId;

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