package com.cu.framework.sample.authcore.trace;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.sql.Connection;
import java.util.Properties;

/**
 * 示例侧的语句观察插件：把每条实际下发到数据库的语句按追踪标识记下来。
 * <p>
 * 顺序上刻意放在 {@code invocation.proceed()} <b>之后</b>再读 BoundSql——
 * 框架的数据范围插件会在下发前改写语句文本，而它改的是同一个 BoundSql 实例，
 * 所以后置读取拿到的是改写后的最终语句。两个插件的先后无论怎么排都成立：
 * 本插件在外层时先放行改写、再读；在内层时改写已经发生、直接读
 * <p>
 * 它与鉴权无关，只服务于"从哪里验证到哪里"：未被声明的语句有没有被改写、
 * 注入的到底是哪个槽位，都要有可查询的落点
 *
 * @author kewen
 * @since 2026-10-20
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class DemoSqlTraceInterceptor implements Interceptor {

    /** 观察台 */
    private DemoSqlTrace demoSqlTrace;

    /**
     * 先放行，再记录最终下发的语句
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object result = invocation.proceed();
        try {
            StatementHandler handler = (StatementHandler) invocation.getTarget();
            String sql = handler.getBoundSql().getSql();
            demoSqlTrace.recordSql("mybatis", sql);
        } catch (Exception e) {
            // 观察失败绝不影响业务执行
            MetaObject metaObject = SystemMetaObject.forObject(invocation.getTarget());
            demoSqlTrace.recordSql("mybatis-unreadable", metaObject.getClass().getName());
        }
        return result;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }

    public void setDemoSqlTrace(DemoSqlTrace demoSqlTrace) {
        this.demoSqlTrace = demoSqlTrace;
    }
}
