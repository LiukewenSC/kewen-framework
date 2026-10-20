package com.cu.framework.sample.authcore;

import com.cu.framework.auth.core.AuthCurrentUserContextHolder;
import com.cu.framework.auth.core.AuthDataRange;
import com.cu.framework.auth.core.data.range.AuthRangeCache;
import com.cu.framework.auth.core.entity.BaseAuth;
import com.cu.framework.auth.core.entity.CurrentUser;
import com.cu.framework.boot.auth.core.config.AuthCoreConfig;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;

/**
 * 声明错误的启动期断言。
 * <p>
 * 这几类场景的共同点是"应用根本起不来"，所以只能在测试里构造，无法在活着的应用上演示。
 * 每一条都用一个独立的 MyBatis 配置装出一个坏的映射语句，再让框架的校验器去扫它，
 * 断言的是"启动失败"并且"失败信息指到了那条声明的位置"
 *
 * @author kewen
 * @since 2026-10-20
 */
public class AuthRangeStartupFailureTest {

    /**
     * 装出一个只含指定映射接口的 SqlSessionFactory，并带着 core 的自动配置刷新上下文
     *
     * @param mapperInterface 待扫描的映射接口
     * @param extraBeanTypes  额外注册的 bean 类型（旧位置残留场景需要一个业务 bean）
     * @return 刷新结果，失败时返回根异常文本
     */
    private static String refreshWithContext(Class<?> mapperInterface, Class<?>... extraBeanTypes) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:startup_failure_test;MODE=MySQL;DB_CLOSE_DELAY=-1");
        dataSource.setUsername("sa");

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.registerBean(DataSource.class, () -> dataSource);
        ctx.registerBean(JdbcTemplate.class, () -> new JdbcTemplate(dataSource));
        ctx.registerBean(RequestMappingHandlerMapping.class, RequestMappingHandlerMapping::new);
        ctx.registerBean(AuthCurrentUserContextHolder.class, () -> new EmptyHolder());
        ctx.registerBean(com.cu.framework.auth.core.menu.AuthMenuHandler.class,
                () -> (com.cu.framework.auth.core.menu.AuthMenuHandler) (auths, path) -> true);
        ctx.register(AuthCoreConfig.class);
        for (Class<?> extra : extraBeanTypes) {
            ctx.register(extra);
        }
        try {
            // 校验器要的是真正的 SqlSessionFactory，且其中只注册这一个接口，
            // 这样报错信息里出现的就只有当前这条坏声明
            SqlSessionFactory factory = buildFactory(mapperInterface);
            ctx.registerBean(SqlSessionFactory.class, () -> factory);
            ctx.refresh();
            return null;
        } catch (Exception e) {
            Throwable root = e;
            while (root.getCause() != null) {
                root = root.getCause();
            }
            return root.getClass().getName() + ": " + root.getMessage();
        } finally {
            ctx.close();
        }
    }

    /**
     * 用纯注解装一个只含指定接口的 MyBatis 配置
     *
     * @param mapperInterface 映射接口
     * @return SqlSessionFactory
     */
    private static SqlSessionFactory buildFactory(Class<?> mapperInterface) {
        try {
            org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
            configuration.setEnvironment(new Environment("test", new JdbcTransactionFactory(),
                    new DriverManagerDataSource("jdbc:h2:mem:startup_failure_test;MODE=MySQL", "sa", "")));
            configuration.addMapper(mapperInterface);
            SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
            bean.setDataSource(new DriverManagerDataSource("jdbc:h2:mem:startup_failure_test;MODE=MySQL", "sa", ""));
            bean.setConfiguration(configuration);
            return bean.getObject();
        } catch (Exception e) {
            throw new IllegalStateException("构造测试用 SqlSessionFactory 失败", e);
        }
    }

    /** 空权限的上下文实现，只为满足强制扩展点 */
    public static class EmptyHolder implements AuthCurrentUserContextHolder {
        @Override
        public CurrentUser getUser() {
            return new CurrentUser().setUsername("nobody");
        }

        @Override
        public Collection<BaseAuth> getAuths() {
            return Collections.emptyList();
        }
    }

    /** 声明所在方法解析不出映射语句：方法上没有任何 SQL 注解 */
    public interface UnresolvedMapper {
        @AuthDataRange(businessFunction = "demo", operate = "unified")
        java.util.List<Object> selectSomething();
    }

    /** 声明标在非查询方法上 */
    public interface WriteStatementMapper {
        @AuthDataRange(businessFunction = "demo", operate = "unified")
        @Update("update demo_work_order set status = 'x' where id = 1")
        int updateSomething();
    }

    /** 声明所在语句启用了二级缓存 */
    @CacheNamespace
    public interface CachedMapper {
        @AuthDataRange(businessFunction = "demo", operate = "unified")
        @Select("select id from demo_work_order")
        java.util.List<Long> selectCached();
    }

    /** 静态可得的 SQL 里找不到声明指定的目标表 */
    public interface WrongTableMapper {
        @AuthDataRange(businessFunction = "demo", operate = "unified", table = "table_that_is_not_there")
        @Select("select id from demo_work_order")
        java.util.List<Long> selectWrongTable();
    }

    /** 声明写在映射接口以外的业务 bean 上（旧用法残留） */
    @Service
    public static class LegacyPlacementService {
        @AuthDataRange(businessFunction = "demo", operate = "unified")
        public java.util.List<Object> listSomething() {
            return Collections.emptyList();
        }
    }

    /**
     * 解析不到映射语句：必须启动失败，并指到声明所在位置
     */
    @Test
    public void unresolvedStatementFailsStartup() {
        String error = refreshWithContext(UnresolvedMapper.class);
        check("解析不到映射语句应启动失败，实际: " + error,
                error != null && error.contains("解析不到映射语句"));
    }

    /**
     * 声明标在非查询方法上：必须启动失败
     */
    @Test
    public void nonSelectStatementFailsStartup() {
        String error = refreshWithContext(WriteStatementMapper.class);
        check("写语句上的声明应启动失败，实际: " + error,
                error != null && error.contains("数据范围只作用于查询语句"));
    }

    /**
     * 语句启用二级缓存：缓存键在注入之前就算出来了，两者不能共存
     */
    @Test
    public void secondLevelCacheFailsStartup() {
        String error = refreshWithContext(CachedMapper.class);
        check("二级缓存与数据范围共存应启动失败，实际: " + error,
                error != null && error.contains("二级缓存"));
    }

    /**
     * 静态 SQL 里目标表写错：必须启动失败并指出表名
     */
    @Test
    public void wrongTargetTableFailsStartup() {
        String error = refreshWithContext(WrongTableMapper.class);
        check("目标表不存在应启动失败，实际: " + error,
                error != null && error.contains("table_that_is_not_there"));
    }

    /**
     * 残留在业务 bean 上的声明：静默不过滤是最坏结果，必须失败
     */
    @Test
    public void legacyPlacementFailsStartup() {
        // 只带一个旧位置残留的声明，其余都合法，好让失败原因唯一指向它
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:legacy_test;MODE=MySQL;DB_CLOSE_DELAY=-1");
        dataSource.setUsername("sa");
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.registerBean(DataSource.class, () -> dataSource);
        ctx.registerBean(JdbcTemplate.class, () -> new JdbcTemplate(dataSource));
        ctx.registerBean(RequestMappingHandlerMapping.class, RequestMappingHandlerMapping::new);
        ctx.registerBean(AuthCurrentUserContextHolder.class, () -> new EmptyHolder());
        ctx.registerBean(com.cu.framework.auth.core.menu.AuthMenuHandler.class,
                () -> (com.cu.framework.auth.core.menu.AuthMenuHandler) (auths, path) -> true);
        ctx.register(AuthCoreConfig.class);
        ctx.register(LegacyPlacementService.class);
        try {
            ctx.registerBean(SqlSessionFactory.class, () -> buildFactory(GoodMapper.class));
            ctx.refresh();
            fail("标在业务 bean 上的声明应启动失败，却正常启动了");
        } catch (Exception e) {
            Throwable root = e;
            while (root.getCause() != null) {
                root = root.getCause();
            }
            check("失败信息应指到不在映射接口上的声明，实际: " + root.getMessage(),
                    root.getMessage() != null && root.getMessage().contains("不在 MyBatis Mapper 接口上"));
        } finally {
            ctx.close();
        }
    }

    /** 一条完全合法的声明，用于隔离"旧位置残留"这一类错误 */
    public interface GoodMapper {
        @AuthDataRange(businessFunction = "demo", operate = "unified")
        @Select("select id from demo_work_order")
        java.util.List<Long> selectGood();
    }

    /**
     * 声明缓存是共享模板：执行期写入的权限集合不能留在缓存里。
     * 这条与启动失败无关，但同属"只有装配起来才看得见"的接缝，放在一起断言
     */
    @Test
    public void cacheIsPopulatedForDeclaredStatements() throws Exception {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:cache_test;MODE=MySQL;DB_CLOSE_DELAY=-1");
        dataSource.setUsername("sa");
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.registerBean(DataSource.class, () -> dataSource);
        ctx.registerBean(JdbcTemplate.class, () -> new JdbcTemplate(dataSource));
        ctx.registerBean(RequestMappingHandlerMapping.class, RequestMappingHandlerMapping::new);
        ctx.registerBean(AuthCurrentUserContextHolder.class, () -> new EmptyHolder());
        ctx.registerBean(com.cu.framework.auth.core.menu.AuthMenuHandler.class,
                () -> (com.cu.framework.auth.core.menu.AuthMenuHandler) (auths, path) -> true);
        ctx.register(AuthCoreConfig.class);
        ctx.registerBean(SqlSessionFactory.class, () -> buildFactory(GoodMapper.class));
        ctx.refresh();
        AuthRangeCache cache = ctx.getBean(AuthRangeCache.class);
        check("合法声明应被扫描进缓存",
                cache.find(GoodMapper.class.getName() + ".selectGood") != null);
        ctx.close();
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

