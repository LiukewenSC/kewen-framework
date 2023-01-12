package com.kewen.framework.quartz.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.quartz.Trigger;

/**
 * @author kewen
 * @descrpition
 * @since 2022-11-15 9:41
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QuartzJobResp extends QuartzJob {

    /**
     * 任务运行状态
     */
    private Trigger.TriggerState triggerState;

}
