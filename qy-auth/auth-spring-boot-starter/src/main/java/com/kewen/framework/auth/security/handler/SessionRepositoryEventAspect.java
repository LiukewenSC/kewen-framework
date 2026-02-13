package com.kewen.framework.auth.security.handler;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.session.Session;
import org.springframework.session.events.SessionCreatedEvent;
import org.springframework.session.events.SessionDestroyedEvent;

/**
 * spring-session 事件切面，原版的是不会触发事件的，到时相关的监听器无法监听session事件
 * @author kewen
 * @since 2024-11-26
 */
@Aspect
public class SessionRepositoryEventAspect implements ApplicationEventPublisherAware {

    private static final Logger log = LoggerFactory.getLogger(SessionRepositoryEventAspect.class);

    ApplicationEventPublisher applicationEventPublisher;

    /**
     * 创建session事件
     * @param returnValue
     */
    @AfterReturning(
            pointcut = "execution(* org.springframework.session.SessionRepository.createSession(..))",
            returning = "returnValue"
    )
    public void createSession(Object returnValue) {
        //createSession
        log.info("Create session");
        if (returnValue instanceof Session) {
            Session session = (Session) returnValue;
            applicationEventPublisher.publishEvent(new SessionCreatedEvent(session,session));
        }
    }

    /**
     * 删除session事件
     * @param pjp
     * @param id
     * @return
     * @throws Throwable
     */
    @Around("execution(* org.springframework.session.SessionRepository.deleteById(..)) && args(id)")
    public Object deleteSession(ProceedingJoinPoint pjp, String id) throws Throwable {
        //deleteById
        log.info("Delete session");
        //
        Object target = pjp.getTarget();

        Session session = (Session) target.getClass().getMethod("findById", String.class).invoke(target, id);

        applicationEventPublisher.publishEvent(new SessionDestroyedEvent(session,session));

        return pjp.proceed();
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
}
