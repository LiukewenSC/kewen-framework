package com.cu.framework.authentication.handler;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.session.events.SessionCreatedEvent;
import org.springframework.session.events.SessionDestroyedEvent;

/**
 * {@link SessionRepository} 的装饰器：把创建/保存/删除等操作委托给原始实现，
 * 并在其上补发 {@link SessionCreatedEvent}、{@link SessionDestroyedEvent}，
 * 使依赖会话事件的监听器（如并发会话控制）能够感知会话变化。
 *
 * <p><strong>为什么需要它：</strong>本框架使用 spring-session-jdbc，会话持久化到数据库。
 * 与 Redis 实现不同，{@code JdbcIndexedSessionRepository} 自身<strong>不会</strong>发布会话事件
 * —— Redis 靠键空间通知 + Pub/Sub 被动感知创建/删除/过期，JDBC 没有等价机制，
 * 因此这里必须主动包装并补发事件。
 *
 * <p><strong>禁止直接以 {@code @Bean} 方式注册本类。</strong>本类实现了 {@link SessionRepository} 接口，
 * 一旦被注册为 SessionRepository 类型的 Bean，就会让 Spring Boot 的 JDBC 会话自动配置整体退避——
 * 其配置类上的 {@code @ConditionalOnMissingBean({ SessionRepository.class, SessionRegistry.class })}
 * 条件不再成立，真正需要被包装的 {@code JdbcIndexedSessionRepository} 根本不会被创建，
 * 本类构造时也就找不到被包装的目标（缺 Bean / 循环依赖）。
 *
 * <p><strong>正确用法：</strong>由
 * {@link com.cu.framework.authentication.config.SessionRepositoryWrapperPostProcessor} 在 Bean
 * 后置处理阶段（{@code postProcessAfterInitialization}）对已经创建好的
 * {@code JdbcIndexedSessionRepository} 就地包装。这样自动配置正常执行、条件不受影响，
 * 下游的 {@code SessionRepositoryFilter} 也会透明地拿到被包装后的实例。
 *
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
