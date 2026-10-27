package com.cu.framework.boot.auth.core.config;

import com.cu.framework.auth.core.AuthCurrentUserContextHolder;
import com.cu.framework.auth.core.entity.FlatteningAuthority;
import com.cu.framework.auth.core.entity.CurrentUser;
import com.cu.framework.auth.core.menu.AuthMenuHandler;
import com.cu.framework.boot.auth.core.AuthExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 拒绝响应通道的自动装配契约。
 * <p>
 * {@code AuthExceptionHandler} 类上只有 {@code @RestControllerAdvice} 注解，既不在 spring.factories
 * 也不在任何使用方的扫描路径里，历史上它永远不会生效——权限拒绝对外退化成 HTTP 500。
 * 这里把"随 starter 生效"钉成断言，同时钉住两条边界：使用方自带同类出口时让位、非 web 环境不装配
 *
 * @author kewen
 * @since 2026-10-20
 */
class AuthExceptionHandlerAssemblyTest {

    private static DataSource h2() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:advice_assembly;MODE=MySQL;DB_CLOSE_DELAY=-1");
        dataSource.setUsername("sa");
        return dataSource;
    }

    /** 满足两个强制扩展点的最小件 */
    private static class StubHolder implements AuthCurrentUserContextHolder {
        @Override
        public CurrentUser getUser() {
            return new CurrentUser().setUsername("stub");
        }

        @Override
        public Collection<FlatteningAuthority> getAuths() {
            return Collections.emptyList();
        }
    }

    private WebApplicationContextRunner webRunner() {
        return new WebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(AuthCoreConfig.class))
                .withBean(DataSource.class, AuthExceptionHandlerAssemblyTest::h2)
                .withBean(JdbcTemplate.class, () -> new JdbcTemplate(h2()))
                .withBean(RequestMappingHandlerMapping.class, RequestMappingHandlerMapping::new)
                .withBean(AuthCurrentUserContextHolder.class, StubHolder::new)
                .withBean(AuthMenuHandler.class, () -> (auths, path) -> true);
    }

    /**
     * web 环境里只引入 starter：拒绝出口必须自动存在，否则规格里"统一拒绝语义"不成立
     */
    @Test
    void registersRejectionChannelWithoutAnyUserConfig() {
        webRunner().run(context ->
                assertEquals(1, context.getBeansOfType(AuthExceptionHandler.class).size(),
                        "框架的拒绝出口应自动装配且只有一个"));
    }

    /**
     * 使用方自己声明了同类出口：框架的默认出口让位，不出现两处竞争
     */
    @Test
    void backsOffWhenUserProvidedTheSameHandler() {
        webRunner()
                .withBean(AuthExceptionHandler.class, AuthExceptionHandler::new)
                .run(context -> assertEquals(1, context.getBeansOfType(AuthExceptionHandler.class).size(),
                        "使用方已声明时框架不应再注册第二个"));
    }

    /**
     * 非 web 环境（没有 MVC 异常出口可言）：不装配该 bean，也不因此启动失败
     */
    @Test
    void notRegisteredOutsideWebApplication() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(AuthCoreConfig.class))
                .withBean(DataSource.class, AuthExceptionHandlerAssemblyTest::h2)
                .withBean(JdbcTemplate.class, () -> new JdbcTemplate(h2()))
                .withBean(RequestMappingHandlerMapping.class, RequestMappingHandlerMapping::new)
                .withBean(AuthCurrentUserContextHolder.class, StubHolder::new)
                .withBean(AuthMenuHandler.class, () -> (auths, path) -> true)
                .run(context -> assertEquals(0, context.getBeansOfType(AuthExceptionHandler.class).size(),
                        "非 web 环境不该装配拒绝响应处理器"));
    }
}
