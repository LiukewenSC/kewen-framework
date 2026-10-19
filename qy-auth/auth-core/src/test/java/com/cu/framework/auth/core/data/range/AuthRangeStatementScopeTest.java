package com.cu.framework.auth.core.data.range;

import com.cu.framework.auth.core.AuthCurrentUserContextHolder;
import com.cu.framework.auth.core.AuthDataRange;
import com.cu.framework.auth.core.entity.BaseAuth;
import com.cu.framework.auth.core.entity.CurrentUser;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 数据范围作用域测试：权限条件只跟着声明所在的语句，不再跟着线程。
 *
 * @author kewen
 * @since 2026-10-19
 */
public class AuthRangeStatementScopeTest {

    private static final String BUSINESS_FUNCTION = "meeting_room";

    /** 声明与未声明、查询与更新混在同一个 mapper 里，用来验证只有声明过的查询语句被管辖 */
    public interface RoomMapper {

        @AuthDataRange(businessFunction = BUSINESS_FUNCTION, operate = "edit_info")
        @Select("select * from meeting_room order by id")
        List<Object> selectEditAble();

        @AuthDataRange(businessFunction = BUSINESS_FUNCTION, operate = "approve")
        @Select("select * from meeting_room where 1 = 1")
        List<Object> selectApproveAble();

        @Select("select * from sys_dict")
        List<Object> selectDict();

        @AuthDataRange(businessFunction = BUSINESS_FUNCTION, operate = "edit_info")
        @Update("update meeting_room set name = 'x' where id = #{id}")
        int updateNamed(@Param("id") Long id);
    }

    private Configuration configuration;
    private AuthRangeCache cache;

    @Before
    public void before() {
        configuration = new Configuration();
        configuration.addMapper(RoomMapper.class);
        cache = new AuthRangeCache();
        cache.scan(configuration);
    }

    /**
     * 声明过的语句进入缓存，未声明的语句不进入；模板内容来自注解且不含权限集合
     */
    @Test
    public void scanCollectsDeclaredStatementsOnly() {
        AuthRange editAble = cache.find(statementId("selectEditAble"));
        Assert.assertNotNull("声明过的查询语句应当命中", editAble);
        Assert.assertEquals(BUSINESS_FUNCTION, editAble.getBusinessFunction());
        Assert.assertEquals("edit_info", editAble.getOperate());
        Assert.assertNull("模板里不允许带任何用户相关数据", editAble.getAuthorities());

        Assert.assertNotNull(cache.find(statementId("selectApproveAble")));
        Assert.assertNull("未声明的查询语句不应命中", cache.find(statementId("selectDict")));
    }

    /**
     * 非查询语句的声明不进入注入缓存，但仍作为声明点保留，交给启动校验判定为错误
     */
    @Test
    public void scanKeepsNonSelectDeclarationOutOfTemplates() {
        Assert.assertNull("写语句不应进入注入缓存", cache.find(statementId("updateNamed")));

        AuthRangeCache.Declaration declaration = findDeclaration("updateNamed");
        Assert.assertNotNull(declaration.getMappedStatement());
        Assert.assertEquals(SqlCommandType.UPDATE, declaration.getMappedStatement().getSqlCommandType());
    }

    /**
     * 回归旧机制 A1：同一条调用链上先执行声明过的查询，未声明的查询不应被注入
     */
    @Test
    public void undeclaredStatementIsNotRewrittenAfterDeclaredOne() {
        StubHolder holder = new StubHolder();
        holder.setAuthorities(adminOnly());
        MybatisDataRangeInterceptor interceptor = interceptorWith(holder);

        Assert.assertNotNull("声明过的语句应当拿到范围定义",
                interceptor.resolveRange(mappedStatement("selectEditAble")));
        Assert.assertNull("紧随其后的未声明语句不应被注入",
                interceptor.resolveRange(mappedStatement("selectDict")));
    }

    /**
     * 回归旧机制 A2：同一语句以不同权限集合连续执行两次，第二次的范围不残留第一次的，
     * 缓存中的模板也不被写回
     */
    @Test
    public void authoritiesAreNotLeakedAcrossCalls() {
        StubHolder holder = new StubHolder();
        holder.setAuthorities(adminOnly());
        MybatisDataRangeInterceptor interceptor = interceptorWith(holder);

        MappedStatement ms = mappedStatement("selectEditAble");
        AuthRange first = interceptor.resolveRange(ms);
        Assert.assertEquals(1, first.getAuthorities().size());

        holder.setAuthorities(Arrays.asList(
                new BaseAuth("ROLE__1", "ROLE__超级管理员"),
                new BaseAuth("ROLE__3", "ROLE__普通角色")));
        AuthRange second = interceptor.resolveRange(ms);
        Assert.assertEquals("第二次调用应拿到当时的权限集合", 2, second.getAuthorities().size());
        Assert.assertEquals("第一次调用的范围不应被改写", 1, first.getAuthorities().size());
        Assert.assertNull("缓存模板不应被写回权限集合", cache.find(statementId("selectEditAble")).getAuthorities());
    }

    private static List<BaseAuth> adminOnly() {
        return Arrays.asList(new BaseAuth("ROLE__1", "ROLE__超级管理员"));
    }

    /**
     * 被其他插件包装过时仍能取到映射语句：MyBatis 3.5.16 的 Plugin 只有 wrap、没有 unwrap，
     * 依赖 MetaObject 沿 h/target 逐层解包，再读 delegate.mappedStatement
     */
    @Test
    public void resolvesMappedStatementThroughPluginProxies() {
        MappedStatement ms = mappedStatement("selectEditAble");
        RoutingStatementHandler handler = new RoutingStatementHandler(
                null, ms, null, RowBounds.DEFAULT, null, ms.getBoundSql(null));

        Object wrapped = new ChainedInterceptor().plugin(new ChainedInterceptor().plugin(handler));
        Assert.assertTrue("应当确实被包成了代理", Proxy.isProxyClass(wrapped.getClass()));

        MybatisDataRangeInterceptor interceptor = new MybatisDataRangeInterceptor();
        Assert.assertSame(ms, interceptor.resolveMappedStatement(interceptor.unwrapPluginProxy(wrapped)));
        Assert.assertSame(ms, interceptor.resolveMappedStatement(interceptor.unwrapPluginProxy(handler)));
    }

    private MybatisDataRangeInterceptor interceptorWith(AuthCurrentUserContextHolder holder) {
        MybatisDataRangeInterceptor interceptor = new MybatisDataRangeInterceptor();
        interceptor.setAuthRangeCache(cache);
        interceptor.setAuthCurrentUserContextHolder(holder);
        return interceptor;
    }

    private MappedStatement mappedStatement(String methodName) {
        return configuration.getMappedStatement(statementId(methodName), false);
    }

    private AuthRangeCache.Declaration findDeclaration(String methodName) {
        String id = statementId(methodName);
        for (AuthRangeCache.Declaration declaration : cache.declarations()) {
            if (id.equals(declaration.getStatementId())) {
                return declaration;
            }
        }
        throw new AssertionError("未找到声明点：" + id);
    }

    private static String statementId(String methodName) {
        return RoomMapper.class.getName() + "." + methodName;
    }

    /** 只提供权限集合的桩实现，权限集合可在测试中替换 */
    private static class StubHolder implements AuthCurrentUserContextHolder {

        private Collection<BaseAuth> authorities = Collections.emptyList();

        void setAuthorities(Collection<BaseAuth> authorities) {
            this.authorities = authorities;
        }

        @Override
        public CurrentUser getUser() {
            return null;
        }

        @Override
        public Collection<BaseAuth> getAuths() {
            return authorities;
        }
    }

    /** 只为制造多层插件包装，本身不做任何处理 */
    @Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
    private static class ChainedInterceptor implements Interceptor {

        @Override
        public Object intercept(Invocation invocation) throws Throwable {
            return invocation.proceed();
        }

        @Override
        public Object plugin(Object target) {
            return Plugin.wrap(target, this);
        }
    }
}
