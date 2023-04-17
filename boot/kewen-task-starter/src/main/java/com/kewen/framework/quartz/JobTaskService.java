package com.kewen.framework.quartz;

import com.kewen.framework.common.core.exception.BizException;
import com.kewen.framework.quartz.job.QuartzJobFactory;
import com.kewen.framework.quartz.mapper.QuartzJobMapper;
import com.kewen.framework.quartz.model.JobStatus;
import com.kewen.framework.quartz.model.QuartzJob;
import com.kewen.framework.quartz.model.QuartzJobResp;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author kewen
 * @descrpition
 * @since 2022-11-15 9:40
 */
@Component
@Slf4j
public class JobTaskService {


    @Autowired
    private Scheduler scheduler;
    @Autowired
    private QuartzJobMapper quartzJobMapper;


    /**
     * 从数据库中取 区别于getAllJob
     *
     * @return
     */
    public List<QuartzJob> getAllDbJob() {
        return quartzJobMapper.selectList(null);
    }

    /**
     * 添加到数据库中 区别于addJob，下次启动才会执行
     */
    public void addTask(QuartzJob job) {
        quartzJobMapper.insert(job);
    }

    /**
     * 从数据库中查询job
     */
    public QuartzJob getJobById(Integer jobId) {
        return quartzJobMapper.selectById(jobId);
    }

    /**
     * 更改任务状态
     *
     * @throws SchedulerException
     */
    public void changeStatus(Integer jobId, JobStatus jobStatus) throws SchedulerException {
        QuartzJob job = getJobById(jobId);
        if (job == null) {
            throw new BizException("未查询到任务");
        }
        job.setStatus(jobStatus);
        quartzJobMapper.updateById(job);
    }

    /**
     * 更改任务 cron表达式
     * @throws SchedulerException
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateCron(Integer jobId, String cron) throws SchedulerException {
        QuartzJob quartzJob = getJobById(jobId);
        if (quartzJob == null) {
            throw new BizException("更新任务失败，未找到任务");
        }
        quartzJob.setCron(cron);
        quartzJobMapper.updateById(quartzJob);

        if (JobStatus.ENABLED== quartzJob.getStatus()) {
            TriggerKey triggerKey = TriggerKey.triggerKey(quartzJob.getJobName(), quartzJob.getJobGroup());
            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cron);
            trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();
            scheduler.rescheduleJob(triggerKey, trigger);
        } else {
            log.info("修改表达式，但未加入quartz中");
        }
    }

    /**
     * 添加任务，添加至corn服务中，及时生效
     *
     * @throws SchedulerException
     */
    private void addOrUpdateQuartzJob(QuartzJob job) throws SchedulerException {
        if (job.getStatus()== JobStatus.DISABLED){
            throw new BizException("被禁用的job无法添加到quartz中");
        }
        log.debug(scheduler + ".......................................................................................add");
        TriggerKey triggerKey = TriggerKey.triggerKey(job.getJobName(), job.getJobGroup());

        CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);

        // 不存在，创建一个
        if (null == trigger) {

            JobDetail jobDetail = JobBuilder.newJob(QuartzJobFactory.class).withIdentity(job.getJobName(), job.getJobGroup()).build();

            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCron());

            trigger = TriggerBuilder.newTrigger()
                    .withIdentity(job.getJobName(), job.getJobGroup())
                    .withSchedule(scheduleBuilder)
                    .build();
            scheduler.scheduleJob(jobDetail, trigger);
        } else {
            // Trigger已存在，那么更新相应的定时设置
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCron()).withMisfireHandlingInstructionFireAndProceed();

            // 按新的cronExpression表达式重新构建trigger
            trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();

            // 按新的trigger重新设置job执行
            scheduler.rescheduleJob(triggerKey, trigger);
        }
    }

    /**
     * 初始化任务
     * @throws Exception
     */
    @PostConstruct
    public void init() throws Exception {
        log.info("加载任务开始");
        // 这里获取任务信息数据
        List<QuartzJob> jobList = quartzJobMapper.selectList(null);

        for (QuartzJob job : jobList) {
            addOrUpdateQuartzJob(job);
        }
        log.info("加载任务完成");
    }

    /**
     * 获取所有计划中的任务列表
     *
     * @return
     * @throws SchedulerException
     */
    public List<QuartzJobResp> getAllQuartzJob() throws SchedulerException {
        GroupMatcher<JobKey> matcher = GroupMatcher.anyJobGroup();
        Set<JobKey> jobKeys = scheduler.getJobKeys(matcher);
        List<QuartzJobResp> jobList = new ArrayList<>();
        for (JobKey jobKey : jobKeys) {
            List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
            for (Trigger trigger : triggers) {
                QuartzJobResp job = new QuartzJobResp();
                job.setJobName(jobKey.getName());
                job.setJobGroup(jobKey.getGroup());
                job.setDescription("触发器:" + trigger.getKey());
                Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
                job.setTriggerState(triggerState);
                job.setStatus(JobStatus.ENABLED);
                if (trigger instanceof CronTrigger) {
                    CronTrigger cronTrigger = (CronTrigger) trigger;
                    String cronExpression = cronTrigger.getCronExpression();
                    job.setCron(cronExpression);
                }
                jobList.add(job);
            }
        }
        return jobList;
    }

    /**
     * 所有正在运行的job
     *
     * @return
     * @throws SchedulerException
     */
    public List<QuartzJobResp> getRunningJob() throws SchedulerException {
        List<JobExecutionContext> executingJobs = scheduler.getCurrentlyExecutingJobs();
        List<QuartzJobResp> jobList = new ArrayList<QuartzJobResp>(executingJobs.size());
        for (JobExecutionContext executingJob : executingJobs) {
            QuartzJobResp job = new QuartzJobResp();
            JobDetail jobDetail = executingJob.getJobDetail();
            JobKey jobKey = jobDetail.getKey();
            Trigger trigger = executingJob.getTrigger();
            job.setJobName(jobKey.getName());
            job.setJobGroup(jobKey.getGroup());
            job.setDescription("触发器:" + trigger.getKey());
            Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
            job.setTriggerState(triggerState);
            if (trigger instanceof CronTrigger) {
                CronTrigger cronTrigger = (CronTrigger) trigger;
                String cronExpression = cronTrigger.getCronExpression();
                job.setCron(cronExpression);
            }
            jobList.add(job);
        }
        return jobList;
    }

    /**
     * 暂停一个job
     *
     * @param quartzJob
     * @throws SchedulerException
     */
    public void pauseJob(QuartzJob quartzJob) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(quartzJob.getJobName(), quartzJob.getJobGroup());
        scheduler.pauseJob(jobKey);
    }

    /**
     * 恢复一个job
     *
     * @param quartzJob
     * @throws SchedulerException
     */
    public void resumeJob(QuartzJob quartzJob) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(quartzJob.getJobName(), quartzJob.getJobGroup());
        scheduler.resumeJob(jobKey);
    }

    /**
     * 删除一个job
     *
     * @param quartzJob
     * @throws SchedulerException
     */
    public void deleteJob(QuartzJob quartzJob) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(quartzJob.getJobName(), quartzJob.getJobGroup());
        scheduler.deleteJob(jobKey);

    }

    /**
     * 立即执行job
     *
     * @param quartzJob
     * @throws SchedulerException
     */
    public void runAJobNow(QuartzJob quartzJob) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(quartzJob.getJobName(), quartzJob.getJobGroup());
        scheduler.triggerJob(jobKey);
    }


}
