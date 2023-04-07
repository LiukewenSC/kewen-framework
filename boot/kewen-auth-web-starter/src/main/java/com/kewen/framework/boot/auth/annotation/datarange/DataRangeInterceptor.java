package com.kewen.framework.boot.auth.annotation.datarange;


import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.parser.SQLExprParser;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.util.JdbcConstants;
import com.alibaba.druid.util.JdbcUtils;
import com.kewen.framework.boot.auth.annotation.CheckDataRange;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.util.Properties;

/**
 * MyBatis 允许你在映射语句执行过程中的某一点进行拦截调用。默认情况下，MyBatis 允许使用插件来拦截的方法调用包括：
 * <p>
 * Executor (update, query, flushStatements, commit, rollback, getTransaction, close, isClosed)
 * ParameterHandler (getParameterObject, setParameters)
 * ResultSetHandler (handleResultSets, handleOutputParameters)
 * StatementHandler (prepare, parameterize, batch, update, query)
 * 这些类中方法的细节可以通过查看每个方法的签名来发现，或者直接查看 MyBatis 发行包中的源代码。 如果你想做的不仅仅是监控方法的调用，那么你最好相当了解要重写的方法的行为。
 * 因为在试图修改或重写已有方法的行为时，很可能会破坏 MyBatis 的核心模块。 这些都是更底层的类和方法，所以使用插件的时候要特别当心。
 * <p>
 * 通过 MyBatis 提供的强大机制，使用插件是非常简单的，只需实现 Interceptor 接口，并指定想要拦截的方法签名即可
 */
@Slf4j
@Component
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class DataRangeInterceptor implements Interceptor {

    @Value("${sys.auth.tableName:sys_application_auth}")
    private String authTableName;
    @Value("${sys.auth.referenceColumn:business_id}")
    private String authReferenceColumn;
    @Value("${sys.auth.authorityColumn:authority}")
    private String authAuthorityColumn;

    /**
     * 所有的sql都会进入此拦截器，需要只校验AuthRangeContext中有数据的，没有数据的说明不是@AuthRange 所拦截的，应当原样输出
     * AuthRangeContext.get()有数据说明一定是从{@link CheckDataRange}拦截的，并在{@link DataRangeAspect}中加入上下文数据且不为空
     * @param invocation
     * @return
     * @throws Throwable
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        DataRangeContext.AuthRange authRangeContext = DataRangeContext.get();
        if (authRangeContext==null){
            log.debug("authRangeContext is empty , do not advance sql ");
            return invocation.proceed();
        }

        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();

        BoundSql boundSql = statementHandler.getBoundSql();
        String sql = boundSql.getSql();
        if (log.isDebugEnabled()){
            log.debug("origin SQL is:" + sql);
        }

        SQLStatementParser sqlParser = SQLParserUtils.createSQLStatementParser(sql, JdbcConstants.MYSQL);
        SQLStatement stmt = sqlParser.parseStatement(false);

        if (stmt instanceof SQLSelectStatement) {

            SQLSelectStatement selectStmt = (SQLSelectStatement) stmt;
            // 拿到SQLSelect 通过在这里打断点看对象我们可以看出这是一个树的结构
            SQLSelect sqlselect = selectStmt.getSelect();
            SQLSelectQueryBlock query = (SQLSelectQueryBlock) sqlselect.getQuery();
            SQLExpr whereExpr = query.getWhere();
            //准备注入sql
            String authWhereCondition = parseAuthWhereConditionSQL(authRangeContext);


            SQLExprParser constraintsParser = SQLParserUtils.createExprParser(authWhereCondition, JdbcUtils.MYSQL);
            SQLExpr constraintsExpr = constraintsParser.expr();
            // 修改where表达式
            if (whereExpr == null) {
                query.setWhere(constraintsExpr);
            } else {
                SQLBinaryOpExpr newWhereExpr = new SQLBinaryOpExpr(
                        whereExpr, SQLBinaryOperator.BooleanAnd, constraintsExpr);
                query.setWhere(newWhereExpr);
            }

            sqlselect.setQuery(query);
            sql = sqlselect.toString();
            //通过反射设置值
            MetaObject metaObject = SystemMetaObject.forObject(boundSql);
            metaObject.setValue("sql",sql);
        }
        // implement post processing if need
        if (log.isDebugEnabled()){
            log.debug("execute sql is:" + sql);
        }
        return invocation.proceed();
    }
    private String parseAuthWhereConditionSQL(DataRangeContext.AuthRange authContext) {
        StringBuilder builder = new StringBuilder();
        for (String authority : authContext.getAuthorities()) {
            builder.append("'").append(authority).append("'").append(",");
        }
        String authSept = builder.substring(0, builder.length() - 1);

        // 组装条件，在原来条件上添加and条件
        // table.primary_id in (select business_id from auth_table where authority in ( 'd_1','r_1' ) )
        // 其中 table.primary_id 则为需要匹配的业务主键ID
        return String.format(" %s.%s in (select %s from %s where module='%s' and operate= '%s' and %s in ( %s ) )",
                authContext.getTableAlias(),
                authContext.getBusinessColumn(),
                authReferenceColumn,
                authTableName,
                authContext.getModule(),
                authContext.getOperate(),
                authAuthorityColumn,
                authSept
        );
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target,this);
        //return Interceptor.super.plugin(target);
    }

    @Override
    public void setProperties(Properties properties) {
    }
}
