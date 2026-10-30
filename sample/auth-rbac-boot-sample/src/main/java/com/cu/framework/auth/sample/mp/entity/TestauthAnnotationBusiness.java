package com.cu.framework.auth.sample.mp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.spring.activerecord.Model;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author kewen
 * @since 2023-12-26
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("testauth_annotation_business")
@ToString
public class TestauthAnnotationBusiness extends Model<TestauthAnnotationBusiness> {

    private static final long serialVersionUID = 1L;

    /** 主键，数据库自增 */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 名称 */
    @TableField("name")
    private String name;

    /** 描述信息 */
    @TableField("description")
    private String description;

    /** 创建时间 */
    @TableField("create_time")
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("is_deleted")
    private Boolean isDeleted;


    @Override
    public Serializable pkVal() {
        return this.id;
    }

}
