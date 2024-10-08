package com.kewen.framework.auth.sys.mp.entity;

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
 * 岗位表
 * </p>
 *
 * @author kewen
 * @since 2023-04-07
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("sys_position")
public class SysPosition extends Model<SysPosition> {

    private static final long serialVersionUID = 1L;

    /**
     * 岗位id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 岗位名
     */
    @TableField("name")
    private String name;

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
