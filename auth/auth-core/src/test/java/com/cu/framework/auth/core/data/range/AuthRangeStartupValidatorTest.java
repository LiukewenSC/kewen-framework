package com.cu.framework.auth.core.data.range;

import com.cu.framework.auth.core.AuthDataRange;
import com.cu.framework.auth.core.data.AuthDataTable;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;

import java.util.List;

/**
 * 数据范围声明的启动校验测试：配置错误必须在启动时就失败，而不是运行期静默放开过滤。
 *
 * @author kewen
 * @since 2026-10-19
 */
public class AuthRangeStartupValidatorTest {

    private static final String BUSINESS_FUNCTION = "meeting_room";

    private SqlAuthInject sqlAuthInject;
    private DefaultListableBeanFactory beanFactory;

    @Before
    public void before() {
        sqlAuthInject = new SqlAuthInject(new AuthDataTable(
                "sys_auth_data", "id", "business_function", "data_id", "operate", "authority", "description"));
        beanFactory = new DefaultListableBeanFactory();
    }

    /** 解析不到映射语句 */
    public interface UnresolvedMapper {
        @AuthDataRange(businessFunction = BUSINESS_FUNCTION)
        List<Object> selectWithoutStatement();
    }

    /** 声明落在写语句上 */
    public interface WriteStatementMapper {
        @AuthDataRange(businessFunction = BUSINESS_FUNCTION)
        @Update("update meeting_room set name = 'x' where id = 1")
        int updateName();
    }

    /** 声明所在语句启用了二级缓存 */
    @CacheNamespace
    public interface CachedMapper {
        @AuthDataRange(businessFunction = BUSINESS_FUNCTION)
        @Select("select * from meeting_room")
        List<Object> selectCached();
    }

    /** 静态 SQL 中不存在声明的目标表 */
    public interface WrongTableMapper {
        @AuthDataRange(businessFunction = BUSINESS_FUNCTION, table = "meeting_room")
        @Select("select * from sys_dict")
        List<Object> selectDict();
    }

    /** 目标表只能运行期拼出，启动期跳过校验 */
    public interface DynamicMapper {
        @AuthDataRange(businessFunction = BUSINESS_FUNCTION, table = "not_exists_yet")
        @Select("<script>select * from meeting_room <if test='true'>where 1 = 1</if></script>")
        List<Object> selectDynamic();
    }

    /** 声明齐全的正常语句 */
    public interface CleanMapper {
        @AuthDataRange(businessFunction = BUSINESS_FUNCTION)
        @Select("select * from meeting_room order by id")
        List<Object> selectClean();
    }

    /** 旧用法：把注解放 in 业务 bean 上 */
    public static class LegacyAnnotatedBean {
        @AuthDataRange(businessFunction = BUSINESS_FUNCTION)
        public List<Object> legacyPage() {
            return null;
        }
    }

    @Test
    public void failsWhenStatementCannotBeResolved() {
        assertFailsWith(UnresolvedMapper.class, "解析不到映射语句");
    }

    @Test
    public void failsWhenStatementIsNotAQuery() {
        assertFailsWith(WriteStatementMapper.class, "只作用于查询语句");
    }

    @Test
    public void failsWhenStatementUsesSecondLevelCache() {
        assertFailsWith(CachedMapper.class, "二级缓存");
    }

    @Test
    public void failsWhenTargetTableIsMissingInStaticSql() {
        assertFailsWith(WrongTableMapper.class, "目标表");
    }

    /**
     * 动态 SQL 不在启动期判定目标表：用 null 参数渲染会真的执行 OGNL，得出一条与实际执行不同的 SQL
     */
    @Test
    public void skipsTargetTableCheckForDynamicSql() {
        validate(DynamicMapper.class);
    }

    @Test
    public void passesForWellConfiguredDeclaration() {
        validate(CleanMapper.class);
    }

    /**
     * 旧用法的注解留在业务 bean 上：切面删除后不会被 mapper 扫描看到，必须靠这条守卫报出来。
     * 用 BeanDefinition 注册，与组件扫描出来的 Controller/Service 在容器里的形态一致
     */
    @Test
    public void failsWhenDeclarationIsNotOnMapperInterface() {
        beanFactory.registerBeanDefinition("legacyBean", new RootBeanDefinition(LegacyAnnotatedBean.class));
        assertFailsWith(CleanMapper.class, "不在 MyBatis Mapper 接口上");
    }

    /** 没有 MyBatis 的场景不应因为缺 SqlSessionFactory 而启动失败 */
    @Test
    public void doesNothingWithoutSqlSessionFactory() {
        AuthRangeCache cache = new AuthRangeCache();
        new AuthRangeStartupValidator(null, cache, sqlAuthInject, beanFactory)
                .afterSingletonsInstantiated();
        Assert.assertTrue(cache.declarations().isEmpty());
    }

    private void assertFailsWith(Class<?> mapperType, String expectedFragment) {
        try {
            validate(mapperType);
        } catch (IllegalStateException e) {
            Assert.assertTrue("异常信息应当包含<" + expectedFragment + ">，实际为：" + e.getMessage(),
                    e.getMessage().contains(expectedFragment));
            return;
        }
        throw new AssertionError("声明错误应当导致启动失败：" + mapperType.getName());
    }

    private void validate(Class<?> mapperType) {
        Configuration configuration = new Configuration();
        configuration.addMapper(mapperType);

        AuthRangeCache cache = new AuthRangeCache();
        new AuthRangeStartupValidator(new DefaultSqlSessionFactory(configuration),
                cache, sqlAuthInject, beanFactory).afterSingletonsInstantiated();
    }

    private static ObjectProvider<SqlSessionFactory> providerOf(SqlSessionFactory factory) {
        return new ObjectProvider<SqlSessionFactory>() {

            @Override
            public SqlSessionFactory getObject() throws BeansException {
                return factory;
            }

            @Override
            public SqlSessionFactory getObject(Object... args) throws BeansException {
                return factory;
            }

            @Override
            public SqlSessionFactory getIfAvailable() throws BeansException {
                return factory;
            }

            @Override
            public SqlSessionFactory getIfUnique() throws BeansException {
                return factory;
            }
        };
    }
}
