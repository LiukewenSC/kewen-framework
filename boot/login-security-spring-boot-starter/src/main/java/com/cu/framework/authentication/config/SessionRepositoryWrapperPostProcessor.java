package com.cu.framework.authentication.config;

import com.cu.framework.authentication.handler.SessionRepositoryEventWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.session.jdbc.JdbcIndexedSessionRepository;

/**
 * 通过 BeanPostProcessor 为 JdbcIndexedSessionRepository 增加事件发布能力。
 * <p>
 * 直接在容器中注册 {@link SessionRepositoryEventWrapper} 作为 Bean 时，
 * 因为它实现了 {@code SessionRepository} 接口，会触发 Spring Session 自动配置的
 * {@code @ConditionalOnMissingBean(SessionRepository.class)} 条件短路，
 * 导致原始 {@code JdbcIndexedSessionRepository} 不被创建，从而产生循环依赖。
 * </p>
 * <p>
 * 本类在 Bean 后处理阶段（{@code postProcessAfterInitialization}）对已创建的
 * {@code JdbcIndexedSessionRepository} 进行包装，既不影响自动配置条件判断，
 * 又能在会话创建/删除时发布对应的 Spring 事件，
 * 使并发会话控制等监听器正常感知会话变化。
 * </p>
 *
 * @author kewen
 * @since 2026-10-16
 * @see SessionRepositoryEventWrapper
 */
public class SessionRepositoryWrapperPostProcessor implements BeanPostProcessor, ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(SessionRepositoryWrapperPostProcessor.class);
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext ctx) {
        this.applicationContext = ctx;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof JdbcIndexedSessionRepository) {
            log.info("增强JdbcIndexedSessionRepository，加入发布事件能力");
            SessionRepositoryEventWrapper wrapper = new SessionRepositoryEventWrapper((JdbcIndexedSessionRepository) bean);
            // 手动触发 Aware 回调
            wrapper.setApplicationEventPublisher(applicationContext);
            return wrapper;
        }
        return bean;
    }
}
