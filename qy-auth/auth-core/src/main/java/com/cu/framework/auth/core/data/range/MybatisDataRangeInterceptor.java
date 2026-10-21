package com.cu.framework.auth.core.data.range;


import com.cu.framework.auth.core.AuthCurrentUserContextHolder;
import com.cu.framework.auth.core.entity.BaseAuth;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.Collection;
import java.util.Properties;

/**
 * 数据范围 SQL 注入拦截器。
 * <p>
 * MyBatis 允许在映射语句执行过程中的某一点进行拦截调用，默认允许拦截的方法签名包括：
 * <p>
 * Executor (update, query, flushStatements, commit, rollback, getTransaction, close, isClosed)
 * ParameterHandler (getParameterObject, setParameters)
 * ResultSetHandler (handleResultSets, handleOutputParameters)
 * StatementHandler (prepare, parameterize, batch, update, query)
 * <p>
 * 本拦截器选择 {@code StatementHandler.prepare}：它是"每条真正执行的物理语句"过一次的位置，
 * 与 {@link com.cu.framework.auth.core.AuthDataRange} 的声明单位（Mapper 接口方法 -> 映射语句）粒度一致，
 * 因此不需要任何跨语句的作用域上下文，被声明的嵌套子查询也各自按自己的语句 id 判定。
 *
 * @author kewen
 * @since 2022-11-23
 */
@Slf4j
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class MybatisDataRangeInterceptor implements Interceptor, ApplicationContextAware {

    /** SQL 权限注入器，未直接注入时首次使用从 Spring 容器懒加载 */
    volatile SqlAuthInject sqlAuthInject;
    /** 数据范围声明缓存，语句 id -> 声明模板 */
    private final AuthRangeCache authRangeCache;
    /** 当前登录用户上下文，执行期取权限集合 */
    private final AuthCurrentUserContextHolder authCurrentUserContextHolder;
    /**
     * Mybatis的插件先于spring容器的完全初始化 ,因此不能直接通过注入等方式获取到容器中的Bean，需要在第一次使用的时候才进行初始化
     * 可以先加载ApplicationContext，再使用时加载AnnotationAuthHandler，
     * 也可以注入的时候添加@Lazy注解
     * @Autowired
     * @Lazy
     * private AnnotationAuthHandler annotationAuthHandler
     */
    private ApplicationContext applicationContext;

    public MybatisDataRangeInterceptor(AuthRangeCache authRangeCache,
                                       AuthCurrentUserContextHolder authCurrentUserContextHolder) {
        this.authRangeCache = authRangeCache;
        this.authCurrentUserContextHolder = authCurrentUserContextHolder;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 获取SQL权限注入器
     * 双重检测锁方式的懒加载
     * @return SQL 权限注入器
     */
    private SqlAuthInject getSqlAuthInject() {
        if (sqlAuthInject == null) {
            synchronized (this) {
                if (sqlAuthInject == null) {
                    sqlAuthInject = applicationContext.getBean(SqlAuthInject.class);
                }
            }
        }

        return sqlAuthInject;
    }

    /**
     * 每条语句都会进入此拦截器，只有语句 id 命中声明缓存的才被增强，其余原样输出。
     * 命中与否完全由 {@link AuthDataRange} 声明在哪个 Mapper 方法上决定，不受调用链上其他查询的影响
     *
     * @param invocation 拦截到的调用
     * @return 原调用结果
     * @throws Throwable 原调用抛出的异常
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object unwrapped = unwrapPluginProxy(invocation.getTarget());
        MappedStatement mappedStatement = resolveMappedStatement(unwrapped);

        AuthRange authRange = resolveRange(mappedStatement);
        if (authRange == null) {
            if (log.isDebugEnabled()) {
                log.debug("语句<{}>未声明数据范围，不做SQL增强处理", mappedStatement.getId());
            }
            return invocation.proceed();
        }

        StatementHandler statementHandler = (StatementHandler) unwrapped;

        BoundSql boundSql = statementHandler.getBoundSql();
        String sql = boundSql.getSql();
        if (log.isDebugEnabled()) {
            log.debug("原始SQL为:{}", sql);
        }
        //获取新的SQL
        String newSql = getSqlAuthInject().convert2NewSql(sql, authRange);

        //通过反射设置值
        MetaObject metaObject = SystemMetaObject.forObject(boundSql);
        metaObject.setValue("sql", newSql);

        if (log.isDebugEnabled()) {
            log.debug("修改之后新的SQL为：{}", newSql);
        }
        return invocation.proceed();
    }

    /**
     * 按语句身份判定该语句要用什么范围注入。
     * <p>
     * 声明模板是缓存中的共享对象，这里必须复制后再写入当次请求的权限集合，
     * 否则会把某个人、某一次请求的权限范围留在缓存里
     *
     * @param mappedStatement 当前语句
     * @return 用于注入的范围定义；该语句未声明数据范围、或不是查询语句时返回 null
     */
    AuthRange resolveRange(MappedStatement mappedStatement) {
        AuthRange template = authRangeCache.find(mappedStatement.getId());
        if (template == null) {
            return null;
        }
        //写语句即使被误标也不改写，声明位置本身已由启动校验判为错误
        if (mappedStatement.getSqlCommandType() != SqlCommandType.SELECT) {
            return null;
        }
        Collection<BaseAuth> auths = authCurrentUserContextHolder.getAuths();
        return template.copy().setAuthorities(auths);
    }

    /**
     * 取当前语句的映射定义。
     * MyBatis 3.5.16 的 {@code Plugin} 只有 {@code wrap}、没有 unwrap，
     * 因此沿插件代理的 {@code h}/{@code target} 字段逐层解包后再读 {@code delegate.mappedStatement}
     * <p>
     * 解析不出来时抛异常而不是放行：识别不出语句身份就无法判断它是否需要过滤，
     * 静默放开正是本次改造要消除的那类失效
     *
     * @param unwrapped 已解开插件包装的 StatementHandler
     * @return 映射语句
     */
    MappedStatement resolveMappedStatement(Object unwrapped) {
        MetaObject metaObject = SystemMetaObject.forObject(unwrapped);
        if (!metaObject.hasGetter("delegate.mappedStatement")) {
            throw new IllegalStateException("无法从 " + unwrapped.getClass().getName()
                    + " 解析 MyBatis 映射语句，数据范围无法判定当前语句，请检查 MyBatis 插件链");
        }
        return (MappedStatement) metaObject.getValue("delegate.mappedStatement");
    }

    /**
     * 逐层解开 {@code Plugin.wrap} 生成的动态代理，拿到真实的 StatementHandler。
     * <p>
     * 只能走 {@link Proxy#getInvocationHandler(Object)} 这个公开入口：JDK 17 下
     * {@code java.lang.reflect.Proxy.h} 字段属于未 open 的 java.base 包，
     * 用 MetaObject 直接读 {@code h} 会抛 InaccessibleObjectException（实测）
     *
     * @param target 拦截到的目标对象
     * @return 解开包装后的目标对象
     */
    Object unwrapPluginProxy(Object target) {
        Object current = target;
        // 防御上限：插件层数远不会到这里，只为避免异常链路下无限循环
        for (int depth = 0; depth < 16 && Proxy.isProxyClass(current.getClass()); depth++) {
            MetaObject handlerMeta = SystemMetaObject.forObject(Proxy.getInvocationHandler(current));
            if (!handlerMeta.hasGetter("target")) {
                break;
            }
            current = handlerMeta.getValue("target");
        }
        return current;
    }

    /**
     * 通过 MyBatis 插件机制包装拦截目标，签名匹配时返回代理对象
     * @param target 待包装的目标对象
     * @return 包装后的对象
     */
    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    /**
     * MyBatis 插件属性配置回调，本拦截器无配置项，空实现
     * @param properties 插件配置的属性
     */
    @Override
    public void setProperties(Properties properties) {
    }

}
