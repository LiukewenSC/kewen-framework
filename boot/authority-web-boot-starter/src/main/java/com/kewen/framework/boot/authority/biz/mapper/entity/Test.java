package com.kewen.framework.boot.authority.biz.mapper.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author kewen
 * @descrpition
 * @since 2022-11-26 20:55
 */
@TableName("test")
@Data
public class Test {

    /**
     *     id         int auto_increment
     *         primary key,
     *     name       varchar(32)      null,
     *     age        tinyint unsigned null,
     *     sex        tinyint          null,
     *     is_deleted bit default b'0' null,
     *     money      float            null
     */

    @TableId
    private Integer id;
    private String name;
    private Integer age;
    private Integer sex;
    private Boolean isDeleted;
    private Double money;

}
