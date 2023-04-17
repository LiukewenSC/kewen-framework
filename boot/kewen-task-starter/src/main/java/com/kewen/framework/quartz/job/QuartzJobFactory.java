package com.kewen.framework.quartz.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kewen.framework.common.core.exception.BizException;
import com.kewen.framework.quartz.mapper.QuartzJobMapper;
import com.kewen.framework.quartz.model.QuartzJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * quartz job执行器，所有的经过执行器的任务都是串行执行
 * 即若相同的job已经在执行了，则等待
 *
 * @author kewen
 * @descrpition
 * @since 2022-11-15 9:32
 */
@DisallowConcurrentExecution
@Component
@Slf4j
public class QuartzJobFactory implements Job, ApplicationContextAware {

    private static final String DEFAULT_METHOD ="scheduledInfo";

    /**
     * 上下文，此处必须使用静态属性，因为 quartz对于job是new的形式，不会加入到spring的管理中，
     *
     * @Component 注解只是为了将上下文注入到类中，因此必须注入到静态属性中
     */
    private static ApplicationContext applicationContext;
    private static QuartzJobMapper quartzJobMapper;
    //private static LogService logService;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        QuartzJobFactory.applicationContext = applicationContext;
        QuartzJobFactory.quartzJobMapper = applicationContext.getBean(QuartzJobMapper.class);
        //com.chinaunicom.cn.emergency.quartz.job.QuartzJobFactory.logService = applicationContext.getBean(LogService.class);
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobKey jobKey = context.getJobDetail().getKey();
        String jobName = jobKey.getName();
        String jobGroup = jobKey.getGroup();
        log.info("启动任务:jobName={},jobGroup={}", jobName, jobGroup);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        QuartzJob quartzJob = quartzJobMapper.selectOne(
                new LambdaQueryWrapper<QuartzJob>()
                        .eq(QuartzJob::getJobName, jobName)
                        .eq(QuartzJob::getJobGroup, jobGroup)
                        .select(QuartzJob::getBeanId, QuartzJob::getBeanClass, QuartzJob::getMethodName,QuartzJob::getDescription)
        );
        Object bean = null;
        if (StringUtils.isNotBlank(quartzJob.getBeanId())) {
            bean = applicationContext.getBean(quartzJob.getBeanId());
        } else if (StringUtils.isNotBlank(quartzJob.getBeanClass())) {
            try {
                bean = applicationContext.getBean(Class.forName(quartzJob.getBeanClass()));
            } catch (ClassNotFoundException e) {
                log.error("反射创建bean失败:" + quartzJob.getBeanClass(), e);
            }
        } else {
            log.error("未配置beanId或beanClass");
            throw new BizException("未配置spring的bean");
        }

        if (bean == null) {
            log.error("任务名称 = [" + jobName + "]---------------未启动成功，请检查是否配置正确！！！");
            return;
        }
        Method method = null;
        try {
            // 只执行不带参数的方法，若需要执行带参数的方法，则需要修改此处
            // method = clazz.getMethod(scheduleJob.getMethodName(), new Class[] {JobExecutionContext.class});
            String methodName = quartzJob.getMethodName();
            if (StringUtils.isBlank(methodName)){
                methodName= DEFAULT_METHOD;
            }
            method = bean.getClass().getMethod(methodName);
        } catch (NoSuchMethodException e) {
            log.error("任务名称 = [" + jobName + "]---------------未启动成功，方法名设置错误！！！", e);

        } catch (SecurityException e) {
            throw new RuntimeException(e);
        }
        if (method==null){
            throw new JobExecutionException("任务执行方法为空");
        }
        LocalDateTime start = LocalDateTime.now();
        boolean success = true;
        Throwable t = null;
        try {
            //method.invoke(bean, new Object[] {context});
            // 只执行不带参数的方法，若需要执行带参数的方法，则需要修改此处
            method.invoke(bean);
        } catch (Throwable throwable) {
            success = false;
            t = throwable;
            log.error("执行任务失败:" + throwable.getMessage(), throwable);
            throw new JobExecutionException(throwable,false);
        } finally {
            //记录日志 controller中切面记录了，此处就不记录了，以后有非Service的再看
            //logService.log("Quartz:"+jobGroup+":"+jobName+":"+quartzJob.getDescription(),start,LocalDateTime.now(),success,t);
            stopWatch.stop();
        }
        double timeSeconds = stopWatch.getTotalTimeSeconds();
        log.info("任务执行完成:jobName={},jobGroup={},耗时{}秒",jobName,jobGroup,timeSeconds);

    }

}
