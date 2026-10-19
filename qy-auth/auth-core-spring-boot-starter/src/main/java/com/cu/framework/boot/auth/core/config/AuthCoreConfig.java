package com.cu.framework.boot.auth.core.config;

import com.cu.framework.auth.core.AuthCurrentUserContextHolder;
import com.cu.framework.auth.core.data.AuthDataHandler;
import com.cu.framework.auth.core.data.JdbcAuthDataHandler;
import com.cu.framework.auth.core.data.JdbcAuthDataPersistent;
import com.cu.framework.auth.core.data.edit.DataCheckAspect;
import com.cu.framework.auth.core.data.AuthDataTable;
import com.cu.framework.auth.core.data.range.AuthRangeCache;
import com.cu.framework.auth.core.data.range.AuthRangeStartupValidator;
import com.cu.framework.auth.core.data.range.MybatisDataRangeInterceptor;
import com.cu.framework.auth.core.data.range.SqlAuthInject;
import com.cu.framework.auth.core.menu.MenuAccessInterceptor;
import com.cu.framework.auth.core.menu.MenuApiGeneratorProcessor;
import com.cu.framework.boot.auth.core.init.InitMenuApiCommandLineRunner;
import com.cu.framework.boot.auth.core.properties.AuthDataTableProperties;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 权限核心core模块所需要的配置
 *
 * @author kewen
 * @since 2023-12-28
 */
@Configuration
@EnableConfigurationProperties(AuthDataTableProperties.class)
public class AuthCoreConfig {


    /*--------------------------------------core.annotation.下配置--------------------------------------*/

    /**
     * 数据校验切面处理
     *
     * @return
     */
    @Bean
    DataCheckAspect dataCheckAspect(AuthDataHandler annotationAuthHandler, AuthCurrentUserContextHolder authCurrentUserContextHolder) {
        DataCheckAspect aspect = new DataCheckAspect();
        aspect.setAnnotationAuthHandler(annotationAuthHandler);
        aspect.setAuthCurrentUserContextHolder(authCurrentUserContextHolder);
        return aspect;
    }

    /**
     * 数据范围声明缓存：以映射语句 id 为键保存注解决定的静态信息。
     * 内容 {@link AuthRangeStartupValidator} 在所有单例就绪后扫描填充
     *
     * @return 声明缓存
     */
    @Bean
    AuthRangeCache authRangeCache() {
        return new AuthRangeCache();
    }

    /**
     * 数据权限处理器。
     * 持久化组件必须显式装配：该类只有 Lombok 的 {@code @Setter}、字段上没有 {@code @Autowired}，
     * 漏设会在读写权限数据时抛空指针
     *
     * @return 数据权限处理器
     */
    @Bean
    JdbcAuthDataHandler jdbcAuthDataHandler(JdbcAuthDataPersistent jdbcAuthDataPersistent) {
        JdbcAuthDataHandler handler = new JdbcAuthDataHandler();
        handler.setJdbcAuthDataPersistent(jdbcAuthDataPersistent);
        return handler;
    }

    /**
     * 数据权限数据库字段
     *
     * @return
     */
    @Bean
    AuthDataTable authDataTable(AuthDataTableProperties authDataTableProperties) {
        AuthDataTable dataTable = new AuthDataTable();

        dataTable.setTableName(authDataTableProperties.getTableName())
                .setIdColumn(authDataTableProperties.getIdColumn())
                .setBusinessFunctionColumn(authDataTableProperties.getBusinessFunctionColumn())
                .setDataIdColumn(authDataTableProperties.getDataIdColumn())
                .setOperateColumn(authDataTableProperties.getOperateColumn())
                .setAuthorityColumn(authDataTableProperties.getAuthorityColumn())
                .setDescriptionColumn(authDataTableProperties.getDescriptionColumn());
        return dataTable;
    }

    /**
     * SQL处理
     *
     * @param authDataTable
     * @return
     */
    @Bean
    SqlAuthInject sqlAuthInject(AuthDataTable authDataTable) {
        return new SqlAuthInject(authDataTable);
    }

    /**
     * mybatis拦截器实现。
     * 该 bean 会在 SqlSessionFactory 构建期间被 MyBatis 取用，因此当前用户上下文用 {@link Lazy} 注入，
     * 避免拦截器 -> 用户上下文 -> composite -> mapper -> SqlSessionFactory 形成创建期循环依赖
     *
     * @return mybatis 数据范围拦截器
     */
    @Bean
    MybatisDataRangeInterceptor mybatisDataRangeInterceptor(AuthRangeCache authRangeCache,
                                                            @Lazy AuthCurrentUserContextHolder authCurrentUserContextHolder) {
        MybatisDataRangeInterceptor interceptor = new MybatisDataRangeInterceptor();
        interceptor.setAuthRangeCache(authRangeCache);
        interceptor.setAuthCurrentUserContextHolder(authCurrentUserContextHolder);
        return interceptor;
    }

    /**
     * 数据范围声明的启动校验器：在所有单例就绪后扫描 Mapper 上的声明、填充 {@link AuthRangeCache}，
     * 并把解析不到语句、语句不是查询、语句启用二级缓存、静态 SQL 中目标表不存在这几类配置错误变成启动失败。
     * 没有 SqlSessionFactory 时（未接入 MyBatis 的场景）跳过
     *
     * @return 启动校验器
     */
    @Bean
    @ConditionalOnBean(SqlSessionFactory.class)
    AuthRangeStartupValidator authRangeStartupValidator(SqlSessionFactory sqlSessionFactory,
                                                        AuthRangeCache authRangeCache,
                                                        SqlAuthInject sqlAuthInject,
                                                        ListableBeanFactory beanFactory) {
        return new AuthRangeStartupValidator(sqlSessionFactory, authRangeCache, sqlAuthInject, beanFactory);
    }


    /**
     * 处理权限数据的批量插入删除
     *
     * @return
     */
    @Bean
    JdbcAuthDataPersistent jdbcAuthDataPersistent(AuthDataTableProperties authDataTableProperties,JdbcTemplate jdbcTemplate) {
        JdbcAuthDataPersistent jdbcAuthDataPersistent = new JdbcAuthDataPersistent();
        jdbcAuthDataPersistent.setJdbcTemplate(jdbcTemplate);
        jdbcAuthDataPersistent.setAuthDataTable(authDataTable(authDataTableProperties));
        return jdbcAuthDataPersistent;
    }


    /*--------------------------------------core.annotation.menu下配置--------------------------------------*/

    /**
     * 菜单访问控制拦截器
     *
     * @return
     */
    @Bean
    MenuAccessInterceptor menuAccessInterceptor() {
        return new MenuAccessInterceptor();
    }

    /**
     * 菜单API生成器
     *
     * @return
     */
    @Bean
    MenuApiGeneratorProcessor menuApiGeneratorProcessor() {
        return new MenuApiGeneratorProcessor();
    }

    /**
     * 菜单API生成调用
     *
     * @return
     */
    @Bean
    InitMenuApiCommandLineRunner initMenuApiCommandLineRunner(MenuApiGeneratorProcessor menuApiGeneratorProcessor) {
        InitMenuApiCommandLineRunner runner = new InitMenuApiCommandLineRunner();
        runner.setMenuApiGeneratorProcessor(menuApiGeneratorProcessor);
        return runner;
    }
}
