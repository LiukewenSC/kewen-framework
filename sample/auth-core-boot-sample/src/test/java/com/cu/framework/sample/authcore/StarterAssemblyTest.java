package com.cu.framework.sample.authcore;

import com.cu.framework.auth.core.AuthCurrentUserContextHolder;
import com.cu.framework.auth.core.entity.FlatteningAuthority;
import com.cu.framework.auth.core.entity.CurrentUser;
import com.cu.framework.auth.core.menu.MenuApiGeneratorProcessor;
import com.cu.framework.boot.auth.core.config.AuthCoreConfig;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;

/**
 * 装配契约测试：三个扩展点里哪两个是强制的、缺了会怎样。
 * <p>
 * 这些结果在正常运行起来的应用上看不到，只能靠"故意不给"来复现
 *
 * @author kewen
 * @since 2026-10-20
 */
public class StarterAssemblyTest {

    /**
     * 只装配 core 的自动配置，身份与语句相关依赖用最小件补齐
     *
     * @param withUserHolder 是否提供当前用户上下文这个扩展点
     * @param withMenuHandler 是否提供菜单权限处理这个扩展点
     * @return 刷新中的上下文调用，由调用方断言成败
     */
    private static AnnotationConfigApplicationContext context(boolean withUserHolder, boolean withMenuHandler) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:assembly_test;MODE=MySQL;DB_CLOSE_DELAY=-1");
        dataSource.setUsername("sa");

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.registerBean(DataSource.class, () -> dataSource);
        ctx.registerBean(JdbcTemplate.class, () -> new JdbcTemplate(dataSource));
        // 非 web 容器里补一个未初始化的映射器即可：生成过程只取它的映射表，空表不触发任何逻辑
        ctx.registerBean(RequestMappingHandlerMapping.class, RequestMappingHandlerMapping::new);
        if (withUserHolder) {
            ctx.registerBean(AuthCurrentUserContextHolder.class, StubUserHolder::new);
        }
        if (withMenuHandler) {
            ctx.registerBean(com.cu.framework.auth.core.menu.AuthMenuHandler.class,
                    () -> (auths, path) -> true);
        }
        ctx.register(AuthCoreConfig.class);
        return ctx;
    }

    /** 只提供框架装配所需的最小行为，不参与任何判定 */
    private static class StubUserHolder implements AuthCurrentUserContextHolder {
        @Override
        public CurrentUser getUser() {
            return new CurrentUser().setUsername("stub");
        }

        @Override
        public Collection<FlatteningAuthority> getAuths() {
            return Collections.emptyList();
        }
    }

    /**
     * A1：缺少当前用户上下文这个强制扩展点，启动必须失败且原因指到它
     */
    @Test
    public void missingUserHolderFailsStartup() {
        AnnotationConfigApplicationContext ctx = context(false, true);
        Exception error = assertThrows(Exception.class, ctx::refresh);
        String chain = describe(error);
        check("失败原因应指到缺失的扩展点，实际是: " + chain,
                chain.contains("AuthCurrentUserContextHolder"));
        ctx.close();
    }

    /**
     * A2：缺少菜单权限处理这个强制扩展点，启动必须失败且原因指到它
     */
    @Test
    public void missingMenuHandlerFailsStartup() {
        AnnotationConfigApplicationContext ctx = context(true, false);
        Exception error = assertThrows(Exception.class, ctx::refresh);
        String chain = describe(error);
        check("失败原因应指到菜单权限处理这个扩展点，实际是: " + chain,
                chain.contains("AuthMenuHandler") || chain.contains("menuAccessInterceptor"));
        ctx.close();
    }

    /**
     * A3：缺少可选的菜单持久化实现，应用照常装配，只是菜单生成被跳过而不是报错
     */
    @Test
    public void missingMenuApiStoreStillStarts() {
        AnnotationConfigApplicationContext ctx = context(true, true);
        try {
            ctx.refresh();
            MenuApiGeneratorProcessor processor = ctx.getBean(MenuApiGeneratorProcessor.class);
            // 容器里没有 MenuApiService，run() 只记录一条跳过日志后返回
            processor.run();
        } catch (Exception e) {
            fail("可选扩展点缺失不应导致启动失败: " + describe(e));
        } finally {
            ctx.close();
        }
    }

    /**
     * 把异常链上的信息拼成一段可断言的文本
     *
     * @param error 顶层异常
     * @return 异常链文本
     */
    private static String describe(Throwable error) {
        StringBuilder builder = new StringBuilder();
        Throwable current = error;
        while (current != null) {
            builder.append(current.getClass().getName()).append(": ").append(current.getMessage()).append(" | ");
            current = current.getCause();
        }
        return builder.toString();
    }
    /**
     * 带说明的断言，参数顺序沿用 JUnit4 习惯
     *
     * @param message   失败时说明
     * @param condition 期望为真
     */
    private static void check(String message, boolean condition) {
        assertTrue(condition, message);
    }
}

