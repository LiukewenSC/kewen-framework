package com.kewen.framework.quartz.model;

import com.baomidou.mybatisplus.annotation.IEnum;

/**
 * job 的启用状态
 * @author kewen
 * @descrpition
 * @since 2022-11-15 9:45
 */
public enum JobStatus implements IEnum<Integer> {
    /**
     * 启用
     */
    ENABLED(1),
    /**
     * 禁用
     */
    DISABLED(2)

    ;

    JobStatus(Integer value) {
        this.value = value;
    }

    private final Integer value;

    @Override
    public Integer getValue() {
        return value;
    }
}
