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
 *
 * @author kewen
 * @since 2026-05-30
 * @deprecated
 * ## 问题根源：与 Spring Session 自动配置的 `@ConditionalOnMissingBean` 冲突
 *
 * 你的项目用的是 `spring-session-jdbc`，它的自动配置类（`JdbcSessionConfiguration`）大致长这样：
 *
 * ```java
 * @Bean
 * @ConditionalOnMissingBean(SessionRepository.class)
 * public JdbcIndexedSessionRepository sessionRepository() {
 *     return new JdbcIndexedSessionRepository(...);
 * }
 * ```
 *
 * 当你用 **Wrapper** 方案时：
 *
 * 1. `SessionRepositoryEventWrapper implements SessionRepository` → 容器中已经有 `SessionRepository` 类型的 Bean 了
 * 2. 自动配置的 `@ConditionalOnMissingBean(SessionRepository.class)` 条件**不满足**，原始的 `JdbcIndexedSessionRepository` **不会被创建**
 * 3. Wrapper 构造器需要注入一个 `SessionRepository`（即被包装的目标）→ **找不到！循环死锁 / NoSuchBeanDefinitionException**
 *
 * ```
 * Wrapper 需要原始 SessionRepository → 但原始的不创建因为 Wrapper 已经是 SessionRepository 了
 * ```
 *
 * ---
 *
 * ## 为什么 Aspect 方案可以正常工作
 *
 * AOP Aspect **不替换 Bean，也不伪装成 Bean 类型**：
 *
 * - `SessionRepositoryEventAspect` 是一个独立的 `@Aspect` Bean，和 `SessionRepository` 接口完全无关
 * - Spring Session 自动配置正常创建 `JdbcIndexedSessionRepository`（条件满足）
 * - Spring AOP 给这个 Bean 生成**动态代理**，所有通过容器调用 `createSession()` / `deleteById()` 的地方都会被拦截
 * - `SessionRepositoryFilter` 内部注入的是代理后的对象，调用自然走切面逻辑
 *
 * ---
 *
 * ## 总结对比
 *
 * | | Aspect (AOP) | Wrapper (装饰器) |
 * |---|---|---|
 * | 是否影响自动配置 | ❌ 不影响，它不是 SessionRepository 类型 | ✅ 影响，它本身实现了 SessionRepository |
 * | Bean 创建 | 原始 Repository 正常创建，AOP 透明代理 | 原始 Repository 因条件不满足而消失 |
 * | 拦截方式 | 代理拦截所有调用（包括 Filter 内部调用） | 需要显式将 Wrapper 注入给所有使用者 |
 *
 * **一句话：Wrapper "不能用"是因为它实现了 `SessionRepository` 接口，触发了 `@ConditionalOnMissingBean` 短路，导致真正要被包装的原始 Bean 压根就没创建出来。**
 */
@Deprecated
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
