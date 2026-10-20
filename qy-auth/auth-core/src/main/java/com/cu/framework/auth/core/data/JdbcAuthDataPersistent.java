package com.cu.framework.auth.core.data;

import lombok.Setter;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcParameter;
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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 权限表 sys_auth_data 的读写。
 * <p>
 * 语句结构一律由 JSqlParser 构造（表、列、条件、值列表都在 AST 上组装），与其他模块保持一致；
 * 但叶子位置放的是 {@link JdbcParameter} 而不是字面量：业务功能、数据ID、操作类型、权限串、权限描述
 * 全部作为参数下发。这样既不需要判断数据ID是什么类型，调用方提供的值也不可能改变查询语义。
 *
 * @author kewen
 * @since 2024-08-14
 */
@Setter
public class JdbcAuthDataPersistent implements InitializingBean {

    /** 执行数据权限 SQL 的 JdbcTemplate */
    private JdbcTemplate jdbcTemplate;

    /** 数据权限表的表名与字段名映射配置 */
    private AuthDataTable authDataTable;

    /**
     * 批量插入：向权限表插入 businessFunction、dataId、operate、authority、description 五列，值全部按参数下发
     *
     * @param datas 待插入的数据权限记录
     * @return 是否有记录写入
     */
    public boolean insertBatchAuthData(Collection<AuthDataDO> datas) {
        if (datas == null || datas.isEmpty()) {
            return false;
        }
        List<Expression> valueTuples = new ArrayList<>(datas.size());
        List<Object> args = new ArrayList<>(datas.size() * 5);
        for (AuthDataDO data : datas) {
            valueTuples.add(new ParenthesedExpressionList(jdbcParameters(5)));
            args.add(data.getBusinessFunction());
            args.add(data.getDataId());
            args.add(data.getOperate());
            args.add(data.getAuthority());
            args.add(data.getDescription());
        }

        Insert insert = new Insert();
        insert.withTable(authDataTable.getTable())
                .withColumns(new ExpressionList().withExpressions(
                        authDataTable.getBusinessFunctionColumn(),
                        authDataTable.getDataIdColumn(),
                        authDataTable.getOperateColumn(),
                        authDataTable.getAuthorityColumn(),
                        authDataTable.getDescriptionColumn()
                ))
                .withSelect(new Values().withExpressions(new ExpressionList(valueTuples)));
        int update = jdbcTemplate.update(insert.toString(), args.toArray());
        return update > 0;
    }

    /**
     * 按权限表主键批量删除，主键就是 {@link #listAuthData} 读回的原值，原样绑定
     *
     * @param ids 主键列表
     * @return 删除的行数
     */
    public int deleteBatchAuthData(List<Object> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        Delete delete = new Delete();
        delete.withTable(authDataTable.getTable())
                .withWhere(new InExpression()
                        .withLeftExpression(authDataTable.getIdColumn())
                        .withRightExpression(new ParenthesedExpressionList(jdbcParameters(ids.size())))
                );
        return jdbcTemplate.update(delete.toString(), ids.toArray());
    }

    /**
     * 根据业务功能和dataID删除数据权限，即清空功能下特定ID的所有权限
     * @param businessFunction 业务功能
     * @param dataId 业务数据ID，类型不限
     * @return 删除的行数
     */
    public int deleteBusinessFunctionDataIdAuth(String businessFunction, Object dataId) {
        Delete delete = new Delete();
        delete.withTable(authDataTable.getTable())
                .withWhere(new AndExpression(
                        new EqualsTo(authDataTable.getBusinessFunctionColumn(), new JdbcParameter()),
                        new EqualsTo(authDataTable.getDataIdColumn(), new JdbcParameter())
                ));
        return jdbcTemplate.update(delete.toString(), businessFunction, dataId);
    }

    /**
     * 根据业务功能、数据ID集合、操作类型查询权限。
     * 只查询 id、data_id、authority、description 四列，数据ID取自查询结果而不是入参，
     * 调用方据此区分命中的究竟是哪条数据
     *
     * @param businessFunction 业务功能
     * @param dataIds 业务数据ID集合
     * @param operate 操作类型
     * @return 权限记录
     */
    public List<AuthDataDO> listAuthData(String businessFunction, Collection<Object> dataIds, String operate) {
        if (dataIds == null || dataIds.isEmpty()) {
            return Collections.emptyList();
        }
        PlainSelect plainSelect = SelectUtils.buildSelectFromTable(authDataTable.getTable()).getPlainSelect();
        plainSelect.withSelectItems(Arrays.asList(
                new SelectItem<>(authDataTable.getIdColumn()),
                new SelectItem<>(authDataTable.getDataIdColumn()),
                new SelectItem<>(authDataTable.getAuthorityColumn()),
                new SelectItem<>(authDataTable.getDescriptionColumn())
        )).withWhere(
                new AndExpression(
                        new AndExpression(
                                new EqualsTo(authDataTable.getBusinessFunctionColumn(), new JdbcParameter()),
                                new EqualsTo(authDataTable.getOperateColumn(), new JdbcParameter())
                        ),
                        new InExpression(authDataTable.getDataIdColumn(),
                                new ParenthesedExpressionList(jdbcParameters(dataIds.size())))
                )
        );

        //参数顺序必须与渲染出的占位符顺序一致：业务功能、操作类型、然后才是 data_id IN (...)
        List<Object> args = new ArrayList<>(dataIds.size() + 2);
        args.add(businessFunction);
        args.add(operate);
        args.addAll(dataIds);

        return jdbcTemplate.query(plainSelect.toString(), args.toArray(), (rs, rowNum) ->
                new AuthDataDO(
                        rs.getObject(1),
                        businessFunction,
                        rs.getObject(2),
                        operate,
                        rs.getString(3),
                        rs.getString(4)));
    }

    /**
     * 校验依赖是否已注入，jdbcTemplate 或 authDataTable 为空时启动失败
     */
    @Override
    public void afterPropertiesSet() {
        if (jdbcTemplate == null || authDataTable == null) {
            throw new IllegalStateException("jdbcTemplate or authDataTable is null");
        }
    }

    /**
     * 生成指定数量的位置参数占位符
     * @param count 占位符个数
     * @return 占位符表达式列表
     */
    private static List<Expression> jdbcParameters(int count) {
        List<Expression> parameters = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            parameters.add(new JdbcParameter());
        }
        return parameters;
    }
}
