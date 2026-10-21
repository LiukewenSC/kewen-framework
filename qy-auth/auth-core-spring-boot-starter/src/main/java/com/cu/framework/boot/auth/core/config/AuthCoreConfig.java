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
import com.cu.framework.auth.core.menu.AuthMenuHandler;
import com.cu.framework.auth.core.menu.MenuAccessInterceptor;
import com.cu.framework.auth.core.menu.MenuApiGeneratorProcessor;
import com.cu.framework.boot.auth.core.AuthExceptionHandler;
import com.cu.framework.boot.auth.core.init.InitMenuApiCommandLineRunner;
import com.cu.framework.boot.auth.core.properties.AuthDataTableProperties;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
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
        return new DataCheckAspect(annotationAuthHandler, authCurrentUserContextHolder);
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
     * 使用方若提供了自己的 {@link AuthDataHandler}（例如基于缓存的实现），本 bean 让位，避免类型冲突
     *
     * @return 数据权限处理器
     */
    @Bean
    @ConditionalOnMissingBean(AuthDataHandler.class)
    JdbcAuthDataHandler jdbcAuthDataHandler(JdbcAuthDataPersistent jdbcAuthDataPersistent) {
        return new JdbcAuthDataHandler(jdbcAuthDataPersistent);
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
        return new MybatisDataRangeInterceptor(authRangeCache, authCurrentUserContextHolder);
    }

    /**
     * 数据范围声明的启动校验器：在所有单例就绪后扫描 Mapper 上的声明、填充 {@link AuthRangeCache}，
     * 并把解析不到语句、语句不是查询、语句启用二级缓存、静态 SQL 中目标表不存在这几类配置错误变成启动失败。
     * <p>
     * 这里不能用 {@code @ConditionalOnBean(SqlSessionFactory.class)}：该条件在配置类的<b>注册阶段</b>求值，
     * 而 MyBatis 自身的自动配置按类名排序可能排在本次之后，条件不成立时校验器根本不会被创建，
     * 结果是声明缓存永远是空的、所有 {@code @AuthDataRange} 静默失效、启动校验也一起消失，
     * 而应用照样启动成功。改成运行期用 {@link ObjectProvider} 取：Bean 定义此时已全部注册，
     * 真没有 SqlSessionFactory（未接入 MyBatis）时 {@code getIfAvailable()} 返回 null，
     * 由校验器自己按那条路径跳过
     *
     * @return 启动校验器
     */
    @Bean
    AuthRangeStartupValidator authRangeStartupValidator(ObjectProvider<SqlSessionFactory> sqlSessionFactoryProvider,
                                                        AuthRangeCache authRangeCache,
                                                        SqlAuthInject sqlAuthInject,
                                                        ListableBeanFactory beanFactory) {
        return new AuthRangeStartupValidator(sqlSessionFactoryProvider.getIfAvailable(),
                authRangeCache, sqlAuthInject, beanFactory);
    }


    /**
     * 处理权限数据的批量插入删除
     *
     * @return
     */
    @Bean
    JdbcAuthDataPersistent jdbcAuthDataPersistent(AuthDataTable authDataTable, JdbcTemplate jdbcTemplate) {
        return new JdbcAuthDataPersistent(jdbcTemplate, authDataTable);
    }


    /*--------------------------------------拒绝响应通道--------------------------------------*/

    /**
     * 权限拒绝的响应通道。
     * 该类只靠 {@code @RestControllerAdvice} 注解存在，不在 spring.factories 里，
     * 使用方不扫描框架包就永远不会生效，菜单与行级判定的拒绝会退化成 HTTP 500，
     * 因此这里显式装配。使用方自己声明了同类处理时本 bean 让位，避免出现两处出口
     *
     * @return 权限异常处理器
     */
    @Bean
    @ConditionalOnWebApplication
    @ConditionalOnMissingBean(AuthExceptionHandler.class)
    AuthExceptionHandler authExceptionHandler() {
        return new AuthExceptionHandler();
    }


    /*--------------------------------------core.annotation.menu下配置--------------------------------------*/

    /**
     * 菜单访问控制拦截器
     *
     * @return
     */
    @Bean
    MenuAccessInterceptor menuAccessInterceptor(AuthMenuHandler authMenuHandler,
                                                AuthCurrentUserContextHolder authCurrentUserContextHolder) {
        return new MenuAccessInterceptor(authMenuHandler, authCurrentUserContextHolder);
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
        return new InitMenuApiCommandLineRunner(menuApiGeneratorProcessor);
    }
}
