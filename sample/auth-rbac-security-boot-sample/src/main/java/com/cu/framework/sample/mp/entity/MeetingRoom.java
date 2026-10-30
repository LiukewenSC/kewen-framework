package com.cu.framework.sample.mp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.spring.activerecord.Model;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author kewen
 * @since 2024-08-06
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("security_sample_meeting_room")
public class MeetingRoom extends Model<MeetingRoom> {

    private static final long serialVersionUID = 1L;

    /** 会议室主键，数据库自增 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 会议室名字
     */
    @TableField("name")
    private String name;

    /**
     *  会议室容纳人数
     */
    @TableField("user_count")
    private Integer userCount;

    /**
     * 位置
     */
    @TableField("place")
    private String place;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 是否有电视屏幕
     */
    @TableField("is_video")
    private Integer isVideo;

    /**
     * 是否有投影仪
     */
    @TableField("is_projector")
    private Integer isProjector;

    /**
     * 是否有座机电话
     */
    @TableField("is_phone")
    private Integer isPhone;

    /** 创建时间 */
    @TableField("create_time")
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField("update_time")
    private LocalDateTime updateTime;


    @Override
    public Serializable pkVal() {
        return this.id;
    }

}
