package com.kewen.framework.quartz.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author kewen
 * @descrpition
 * @since 2022-11-15 9:41
 */
@Data
@TableName("quartz_job")
public class QuartzJob {
    /**
     * 主键ID
     */
    @TableId
    protected Integer id;
    /**
     * 任务名
     */
    protected String jobName;
    /**
     * 任务组
     */
    protected String jobGroup;
    /**
     * cron 表达式
     */
    protected String cron;
    /**
     * 描述
     */
    protected String description;
    /**
     * spring中的bean
     */
    protected String beanClass;
    /**
     * spring 中的ID，当有多个beanClass时需要用ID区分
     */
    protected String beanId;
    /**
     * 执行方法 默认为空则执行 scheduledInfo() 方法
     */
    protected String methodName;
    /**
     * 状态 1-启用 2-停用
     */
    protected JobStatus status;
    /**
     * 是否初始化后立即执行
     */
    protected Boolean initStarted;
    /**
     * 创建时间
     */
    protected LocalDateTime createTime;
    /**
     * 更新时间
     */
    protected LocalDateTime updateTime;
}
