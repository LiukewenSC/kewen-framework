package com.cu.framework.authentication.handler;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.session.events.SessionCreatedEvent;
import org.springframework.session.events.SessionDestroyedEvent;

/**
 * SessionRepository 装饰器：委托原始 SessionRepository 完成会话操作，
 * 并在创建/保存/删除后额外发布 SessionCreatedEvent、SessionDestroyedEvent，
 * 使依赖会话事件的监听器（如并发会话控制）能够感知变化
 * @author kewen
 * @since 2026-05-30
 */
public class SessionRepositoryEventWrapper implements SessionRepository, ApplicationEventPublisherAware {

    /** 被装饰的原始 SessionRepository */
    SessionRepository wrapper;

    public SessionRepositoryEventWrapper(SessionRepository wrapper) {
        this.wrapper = wrapper;
    }

    /** Spring 事件发布器，由容器注入 */
    ApplicationEventPublisher applicationEventPublisher;


    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * 委托创建会话，成功后发布 SessionCreatedEvent
     */
    @Override
    public Session createSession() {
        Session session = wrapper.createSession();
        applicationEventPublisher.publishEvent(new SessionCreatedEvent(session,session));
        return session;
    }

    /**
     * 委托保存会话，保存后发布 SessionCreatedEvent
     */
    @Override
    public void save(Session session) {
        wrapper.save(session);
        applicationEventPublisher.publishEvent(new SessionCreatedEvent(session,session));
    }

    @Override
    public Session findById(String id) {
        Session session = wrapper.findById(id);
        return session;
    }

    /**
     * 先按 ID 查出会话再委托删除，随后发布会话上的 SessionDestroyedEvent
     */
    @Override
    public void deleteById(String id) {
        Session session = wrapper.findById(id);
        wrapper.deleteById(id);
        applicationEventPublisher.publishEvent(new SessionDestroyedEvent(session, session));
    }
}
