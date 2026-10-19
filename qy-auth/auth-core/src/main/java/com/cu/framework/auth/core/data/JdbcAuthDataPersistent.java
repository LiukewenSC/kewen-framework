package com.cu.framework.auth.core.data;

import com.cu.framework.auth.core.exception.AuthServiceException;
import lombok.Setter;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.Values;
import net.sf.jsqlparser.util.SelectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author kewen
 * @since 2024-08-14
 */
@Setter
public class JdbcAuthDataPersistent implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(JdbcAuthDataPersistent.class);
    /** 执行数据权限 SQL 的 JdbcTemplate */
    private JdbcTemplate jdbcTemplate;

    /** 数据权限表的表名与字段名映射配置 */
    private AuthDataTable authDataTable;

    /**
     * 批量插入
     *
     * @param datas
     * @return
     */
    public boolean insertBatchAuthData(Collection<AuthDataDO> datas) {
        String sql = buildInsertBatchSql(datas);
        int update = jdbcTemplate.update(sql);
        return update > 0;
    }

    /**
     * 构造批量插入 SQL：向权限表插入 businessFunction、dataId、operate、authority、description 五列
     * @param datas 待插入的数据权限记录
     * @return 拼接完成的 insert 语句
     */
    private String buildInsertBatchSql(Collection<AuthDataDO> datas) {
        Insert insert = new Insert();
        insert.withTable(authDataTable.getTable())
                .withColumns(new ExpressionList().withExpressions(
                        authDataTable.getBusinessFunctionColumn(),
                        authDataTable.getDataIdColumn(),
                        authDataTable.getOperateColumn(),
                        authDataTable.getAuthorityColumn(),
                        authDataTable.getDescriptionColumn()
                ))
                .withSelect(new Values().withExpressions(new ExpressionList(
                        datas.stream().map(d -> new ParenthesedExpressionList(
                                new StringValue(d.getBusinessFunction()),
                                new StringValue(d.getDataId().toString()),
                                new StringValue(d.getOperate()),
                                new StringValue(d.getAuthority()),
                                new StringValue(d.getDescription())
                        )).collect(Collectors.toList())
                )));
        return insert.toString();
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    public <Object> int deleteBatchAuthData(List ids) {
        String sql = removeByIdsSql(ids);
        return jdbcTemplate.update(sql);
    }

    /**
     * 构造按主键批量删除的 SQL（id IN (...)），Long / Integer 转为数值字面量，StringValue 转为字符串字面量，其余类型抛出异常
     * @param ids 主键列表
     * @return 拼接完成的 delete 语句
     * @throws RuntimeException ID 类型不受支持时抛出
     */
    private  String removeByIdsSql(List ids) {
        ArrayList<Expression> expressions = new ArrayList<>();
        for (Object id : ids) {
            if (id instanceof Long) {
                Long id1 = (Long) id;
                expressions.add(new LongValue(id1));
            } else if (id instanceof Integer) {
                Integer id1 = (Integer) id;
                expressions.add(new LongValue(id1));
            } else if (id instanceof String) {
                expressions.add(new StringValue(id.toString()));
            } else {
                throw new RuntimeException("ID类型异常");
            }
        }
        Delete delete = new Delete();
        delete.withTable(authDataTable.getTable())
                .withWhere(new InExpression()
                        .withLeftExpression(authDataTable.getIdColumn())
                        .withRightExpression(new ParenthesedExpressionList(expressions))
                );
        return delete.toString();
    }

    /**
     * 根据业务功能和dataID删除数据权限，即清空功能下特定ID的所有权限
     * @param businessFunction
     * @param dataId
     * @param <Object>
     * @return
     */
    public <Object> int deleteBusinessFunctionDataIdAuth(String businessFunction, Object dataId) {
        String sql = removeBusinessFunctionDataIdSql(businessFunction,dataId);
        return jdbcTemplate.update(sql);
    }
    /**
     * 构造按 业务功能 + 数据ID 删除权限记录的 SQL
     * @param businessFunction 业务功能
     * @param dataId 数据ID，仅支持 String / Long / Integer 类型
     * @return 拼接完成的 delete 语句
     * @throws AuthServiceException dataId 类型不受支持时抛出
     */
    private <Object> String removeBusinessFunctionDataIdSql(String businessFunction, Object dataId) {
        Delete delete = new Delete();

        Expression dataIdExpression;
        if (dataId instanceof String){
            dataIdExpression = new StringValue(dataId.toString());
        } else if (dataId instanceof Long){
            dataIdExpression = new LongValue(dataId.toString());
        } else if (dataId instanceof Integer){
            dataIdExpression = new LongValue(dataId.toString());
        } else {
            throw new AuthServiceException("ID类型不正确");
        }

        delete.withTable(authDataTable.getTable())
                .withWhere(
                        new AndExpression(
                                new EqualsTo(authDataTable.getBusinessFunctionColumn(),new StringValue(businessFunction)),
                                new EqualsTo(authDataTable.getDataIdColumn(), dataIdExpression)
                        )
                );
        return delete.toString();
    }


    /**
     * 校验依赖是否已注入，jdbcTemplate 或 authDataTable 为空时启动失败
     * @throws Exception 依赖缺失时抛出 RuntimeException
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        if (jdbcTemplate == null || authDataTable == null) {
            throw new RuntimeException("jdbcTemplate or authDataTable is null");
        }
    }

    /**
     * 根据业务方法，数据ID，操作类型查询权限
     *
     * @param businessFunction
     * @param dataId
     * @param operate
     * @return
     */
    public List<AuthDataDO> listAuthData(String businessFunction, Object dataId, String operate) {
        String sql = listAuthDataSql(businessFunction, dataId, operate);

        List<AuthDataDO> list = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Object id;
            if (dataId instanceof Long) {
                id = Long.valueOf(rs.getLong(1));
            } else if (dataId instanceof Integer) {
                id = Integer.valueOf(rs.getInt(1));
            } else if (dataId instanceof String) id = (Object) rs.getString(1);
            else {
                log.error("ID类型不正确 {}", rs);
                id = null;
            }
            String authority = rs.getString(2);
            String description = rs.getString(3);
            return new AuthDataDO(id, businessFunction, dataId, operate, authority, description);
        });
        if (list == null) {
            return Collections.emptyList();
        }
        return list;

    }

    /**
     * 构造按 业务功能 + 数据ID + 操作类型 查询权限的 SQL，只查询 id、authority、description 三列
     * @param businessFunction 业务功能
     * @param dataId 数据ID
     * @param operate 操作类型
     * @return 拼接完成的 select 语句
     */
    private <Object> String listAuthDataSql(String businessFunction, Object dataId, String operate) {
        PlainSelect plainSelect = SelectUtils.buildSelectFromTable(authDataTable.getTable()).getPlainSelect();
        plainSelect.withWhere(
                new AndExpression(
                        new AndExpression(
                                new EqualsTo(authDataTable.getBusinessFunctionColumn(), new StringValue(businessFunction)),
                                new EqualsTo(authDataTable.getDataIdColumn(), new StringValue(dataId.toString()))
                        ),
                        new EqualsTo(authDataTable.getOperateColumn(), new StringValue(operate))
                )
        ).withSelectItems(Arrays.asList(
                new SelectItem<>(authDataTable.getIdColumn()),
                new SelectItem<>(authDataTable.getAuthorityColumn()),
                new SelectItem<>(authDataTable.getDescriptionColumn())
        ))
        ;
        return plainSelect.toString();
    }
}
