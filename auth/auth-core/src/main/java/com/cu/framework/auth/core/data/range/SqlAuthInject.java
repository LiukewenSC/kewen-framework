package com.cu.framework.auth.core.data.range;

import com.cu.framework.auth.core.data.AuthDataTable;
import com.cu.framework.auth.core.entity.FlatteningAuthority;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.util.SelectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * auth对应的SQL注入器，注入权限查询条件
 * 构造如下的SQL语句
 * SELECT * FROM meeting_room
 * WHERE id IN (
 * SELECT data_id FROM sys_auth_data
 * WHERE authority IN ('ROLE_1', 'USER_kewen')
 * AND business_function = 'meeting_room'
 * AND operate = 'edit'
 * );
 * 或者
 * SELECT * FROM meeting_room
 * WHERE EXISTS (
 * SELECT 1 FROM sys_auth_data
 * WHERE id = data_id
 * AND authority IN ('ROLE_1', 'USER_kewen')
 * AND business_function = 'meeting_room'
 * AND operate = 'edit'
 * )
 *
 * 同时，现在也支持子查询了
 * @author kewen
 * @since 2024-08-08
 */
public class SqlAuthInject {

    private static final Logger log = LoggerFactory.getLogger(SqlAuthInject.class);

    /** 权限集合为空时注入的恒假条件 */
    private static final String ALWAYS_FALSE_CONDITION = "1 = 0";

    /** 数据权限表的表名与字段名映射配置，注入的权限子查询以此表构造 */
    AuthDataTable authDataTable;

    public SqlAuthInject(AuthDataTable authDataTable) {
        this.authDataTable = authDataTable;
    }

    /**
     * 转换成新的SQL
     */
    public String convert2NewSql(String sql, AuthRange authRange) throws JSQLParserException {
        Select parse = (Select) CCJSqlParserUtil.parse(sql);
        PlainSelect plainSelect = parse.getPlainSelect();

        //构造原始表
        Table table = buildOriginalTable(authRange);

        //解析SQL，要做子查询，这里需要分析二叉树的各种逻辑，因为子查询可以在嵌套的各个地方，而Jsql所有的条件都是二叉构造
        PlainSelectAndTable plainSelectAndTable = findAuthPlainSelect(plainSelect, table);

        if (plainSelectAndTable ==null){
            throw new JSQLParserException(String.format("没有匹配到SQL<%s>对应的表<%s,%s>，请检查",sql,authRange.getTable(),authRange.getTableAlias()));
        }

        //注入SQL
        injectSql(authRange, plainSelectAndTable);

        return plainSelect.toString();
    }

    /**
     * 校验 SQL 中能定位到声明指定的目标表，供启动期校验静态可得的 SQL。
     * 与 {@link #convert2NewSql} 复用同一套表匹配规则，只解析不改写、不触碰权限集合
     *
     * @param sql       待校验的 SQL
     * @param authRange 声明模板（不含权限集合）
     * @throws JSQLParserException SQL 解析失败，或匹配不到声明的目标表
     */
    public void validateTargetTable(String sql, AuthRange authRange) throws JSQLParserException {
        Select parse = (Select) CCJSqlParserUtil.parse(sql);
        PlainSelect plainSelect = parse.getPlainSelect();

        if (findAuthPlainSelect(plainSelect, buildOriginalTable(authRange)) == null) {
            throw new JSQLParserException(String.format("没有匹配到SQL<%s>对应的表<%s,%s>，请检查",
                    sql, authRange.getTable(), authRange.getTableAlias()));
        }
    }

    /**
     * 构造原始表， 需要有tableName才有alias
     * @param authRange
     * @return
     */
    private static Table buildOriginalTable(AuthRange authRange) {
        // 构造需要查询的表，当tableName为空时，Table里面为空表
        String tableName = authRange.getTable();
        Table table = new Table(tableName);
        //没有tableName也没有alias
        if (StringUtils.isNotBlank(tableName) && StringUtils.isNotBlank(authRange.getTableAlias())){
            table.setAlias(new Alias(authRange.getTableAlias()));
        }
        return table;
    }

    /**
     * 注入SQL
     */
    private void injectSql(AuthRange authRange, PlainSelectAndTable plainSelectAndTable) throws JSQLParserException {
        //找到对应的 PlainSelect 和 Table，修改SQL
        Table authTable = plainSelectAndTable.table;
        PlainSelect authPlainSelect = plainSelectAndTable.plainSelect;
        Expression condition;
        if (authRange.getAuthorities() == null || authRange.getAuthorities().isEmpty()) {
            //权限集合为空 = 看不到任何行，与 AuthDataHandler.hasDataOperateAuths 的空集合即拒绝同一语义。
            // 这里不能照样构造权限子查询：空值列表会渲染成 `authority in ( )`，整条语句直接不成立
            condition = parseAlwaysFalseExpression();
        } else if (authRange.getMatchMethod() == MatchMethod.IN) {
            //获取到 `id in (a,b,c)` 表达式
            condition = parseIdInExpression(authRange, authTable);
        } else if (authRange.getMatchMethod() == MatchMethod.EXISTS) {
            condition = parseIdExistsExpression(authRange, authTable);
        } else {
            throw new RuntimeException("仅支持 in 和 exists");
        }
        Expression where = authPlainSelect.getWhere();
        if (where == null) {
            authPlainSelect.setWhere(condition);
        } else {
            //组合现在的和原来的
            authPlainSelect.setWhere(new AndExpression(where, condition));
        }
    }

    /**
     * 构造恒假条件，用于权限集合为空时替代权限子查询。
     * 语句仍然带着过滤条件，只是恒不成立，因此返回零行——既不是放开过滤，也不是语法错误
     *
     * @return 恒假表达式
     * @throws JSQLParserException 常量条件解析失败
     */
    private Expression parseAlwaysFalseExpression() throws JSQLParserException {
        return CCJSqlParserUtil.parseCondExpression(ALWAYS_FALSE_CONDITION);
    }

    /**
     * 查找到最终匹配条件的 PlainSelect
     *
     * @param plainSelect
     * @return
     */
    private PlainSelectAndTable findAuthPlainSelect(PlainSelect plainSelect, Table paramTable) throws JSQLParserException {
        //首先查询最外层，最外层可以是没有 名字或别名的
        FromItem fromItem = plainSelect.getFromItem();
        if (fromItem instanceof Table){
            Table currentTable = (Table) fromItem;
            //没有配置表名或者匹配上当前的表，则直接返回主表了
            if (StringUtils.isBlank(paramTable.getName()) || matchesTable(currentTable,paramTable)) {
                return new PlainSelectAndTable(plainSelect,currentTable);
            }
        }

        //再看看有没有with字句，有就查看with
        List<WithItem> withItemsList = plainSelect.getWithItemsList();
        if (withItemsList !=null){
            for (WithItem withItem : withItemsList) {
                PlainSelect withPlainSelect = withItem.getSelect().getPlainSelect();
                PlainSelectAndTable plainSelectAndTable = parsePlainSelectThenWhere(withPlainSelect, paramTable);
                if (plainSelectAndTable != null) {
                    return plainSelectAndTable;
                }
            }
        }

        //没有匹配到则按照普通的来匹配
        return parsePlainSelectThenWhere(plainSelect, paramTable);

    }

    /**
     * 在条件表达式中递归查找包含目标表的 PlainSelect：
     * Select 子查询直接递归；And / Or 依次查找左右表达式；In / EqualsTo 查找右侧表达式；
     * 其他表达式类型不做处理
     * @param expression 待查找的条件表达式
     * @param paramTable 目标表（可带别名）
     * @return 命中的 PlainSelect 与表的组合，未找到返回 null
     * @throws JSQLParserException 递归解析子查询出错时抛出
     */
    private PlainSelectAndTable parseExpression(Expression expression, Table paramTable) throws JSQLParserException {
        if (expression == null) {
            //没有 WHERE 子句时无处可找，交由调用方按未匹配处理
            return null;
        }
        PlainSelectAndTable psat = null;
        if (expression instanceof Select) {
            //这是递归中唯一满足要求提前结束的场景，按照PlainSelect查询匹配，
            // 如果匹配上了则说明找到了，plainSelect，否则继续递归或结束
            Select select = (Select) expression;
            PlainSelect plainSelect = select.getPlainSelect();
            //此处递归了
            psat = parsePlainSelectThenWhere(plainSelect, paramTable);
        } else if (expression instanceof AndExpression) {
            //and方式需要左右都查找
            AndExpression andExpression = (AndExpression) expression;
            psat = parseExpression(andExpression.getLeftExpression(), paramTable);
            if (psat == null) {
                psat = parseExpression(andExpression.getRightExpression(), paramTable);
            }
        } else if (expression instanceof OrExpression) {
            //or也需要左右都查找
            OrExpression orExpression = (OrExpression) expression;
            psat = parseExpression(orExpression.getLeftExpression(), paramTable);
            if (psat == null) {
                psat = parseExpression(orExpression.getRightExpression(), paramTable);
            }
        } else if (expression instanceof InExpression) {
            //in需要查找右侧
            InExpression inExpression = (InExpression) expression;
            psat = parseExpression(inExpression.getRightExpression(), paramTable);
        } else if (expression instanceof EqualsTo) {
            EqualsTo equalsTo = (EqualsTo) expression;
            psat = parseExpression(equalsTo.getRightExpression(), paramTable);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("没有做表达式<{}>匹配的场景，到了这里就说明内部无法查找出PlainSelect，也就无法递归出需要的表",
                        expression.getClass().getName()
                );
            }
        }
        return psat;
    }

    /**
     * 从当前 PlainSelect 中找，没有找到则找where中的，实际上是会递归
     * @param plainSelect
     * @param paramTable
     * @return
     * @throws JSQLParserException
     */
    private PlainSelectAndTable parsePlainSelectThenWhere(PlainSelect plainSelect, Table paramTable) throws JSQLParserException {
        PlainSelectAndTable plainSelectAndTable = parseCurrentPlainSelect(plainSelect, paramTable);
        if (plainSelectAndTable !=null){
            return plainSelectAndTable;
        }
        //主查询没有匹配上则查找where中子查询
        return parseExpression(plainSelect.getWhere(), paramTable);
    }
    /**
     * 从 PlainSelect 中找到匹配的表，主要是 从 From Join中找
     * 找到了则说明当前PlainSelect就是需要处理的
     */
    private PlainSelectAndTable parseCurrentPlainSelect(PlainSelect plainSelect, Table paramTable) throws JSQLParserException {


        //判断本查询主表是否满足
        FromItem fromItem = plainSelect.getFromItem();
        if (fromItem instanceof Table) {
            final Table currentTable = (Table) fromItem;
            if (matchesTable(currentTable,paramTable)) {
                return new PlainSelectAndTable(plainSelect,currentTable);
            }
        }

        //不满足再看关联表是否满足
        if (plainSelect.getJoins() != null) {
            for (Join join : plainSelect.getJoins()) {
                FromItem joinFromItem = join.getFromItem();
                if (joinFromItem instanceof Table) {
                    Table joinTable = ((Table) joinFromItem);
                    boolean matches = matchesTable(joinTable, paramTable);
                    if (matches){
                        return new PlainSelectAndTable(plainSelect,joinTable);
                    }
                }
            }
        }
        if (log.isDebugEnabled()){
            log.debug("SQL<{}>中没有找到表名<{}>对应的表",plainSelect,paramTable);
        }
        return null;
    }
    /**
     * 判断当前表与目标表是否匹配：表名相等即匹配；目标表带别名时还要求别名相等
     * @param current SQL 中的当前表
     * @param paramTable 注解配置构造的目标表
     * @return 是否匹配
     */
    private boolean matchesTable(Table current, Table paramTable) {
        String name = paramTable.getName();
        String alias = paramTable.getAlias() == null ? null : paramTable.getAlias().getName();
        if (name.equals(current.getName())){
            //如果需要找的表别名为空，那么直接表名匹配就可以了
            if (alias ==null){
                return true;
            } else {
                //如果表别名不为空，则需要判断 表别名是否相等
                if (current.getAlias() != null){
                    if (alias.equals(current.getAlias().getName())){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 构造 id in () 条件表达式
     * table.id in (select data_id from sys_auth_data where authority in ( 'd_1','r_1' ) )
     * 其中 table.id 则为需要匹配的业务主键ID
     */
    private Expression parseIdInExpression(AuthRange authRange, Table authTable) throws JSQLParserException {

        //1 构造 select data_id from sys_auth_data
        // where
        // business_function=#{meeting_room} and operate=#{edit} and authority in ( 'd_1','r_1' )

        //构建 select data_id from sys_auth_data
        //Select authSqlSelect = ((Select) CCJSqlParserUtil.parse("select data_id from sys_auth_data"));
        Select authSqlSelect = SelectUtils.buildSelectFromTableAndSelectItems(
                authDataTable.getTable(),
                new SelectItem<>(authDataTable.getDataIdColumn())
        );
        PlainSelect authSqlPlainSelect = authSqlSelect.getPlainSelect();
        // 得到权限表的条件
        AndExpression inAndBusinessFunctionAndOperateExpression = authTableWhereExpression(authRange);
        //2 设置where条件
        authSqlPlainSelect.setWhere(inAndBusinessFunctionAndOperateExpression);

        //3 构造外层 table.id in (#{authSqlSelect})
        String tableName = parseAliseOrTableName(authTable);
        //需要将authSqlSelect包裹在括号中
        ParenthesedSelect parenthesedSelect = new ParenthesedSelect();
        parenthesedSelect.setSelect(authSqlSelect);

        InExpression inExpression = new InExpression(new Column(tableName + "." + authRange.getDataIdColumn()), parenthesedSelect);

        //返回最后结果
        return inExpression;
    }

    /**
     * 构造 exists 条件表达式
     * exists ( select 1 from sys_auth_data where authority in ( 'd_1','r_1' ) and table.id=data_id )
     */
    private ExistsExpression parseIdExistsExpression(AuthRange authRange, Table mainTable) throws JSQLParserException {
        ExistsExpression existsExpression = new ExistsExpression();
        // 构造 select 1 from sys_auth_data
        // PlainSelect plainSelect = ((Select) CCJSqlParserUtil.parse("select 1 from sys_auth_data")).getPlainSelect();
        PlainSelect plainSelect = SelectUtils.buildSelectFromTableAndSelectItems(
                authDataTable.getTable(),
                new SelectItem<>(new Column("1"))
        ).getPlainSelect();
        AndExpression authedWhereExpression = authTableWhereExpression(authRange);
        //Expression equalsTo = CCJSqlParserUtil.parseCondExpression("id=data_id");
        String tableName = parseAliseOrTableName(mainTable);
        Expression equalsTo = new EqualsTo(getMainTableColumn(tableName, authRange.getDataIdColumn()),authDataTable.getDataIdColumn());
        AndExpression where = new AndExpression(equalsTo, authedWhereExpression);
        plainSelect.setWhere(where);
        //需要将plainSelect包裹在括号中
        ParenthesedSelect parenthesedSelect = new ParenthesedSelect();
        parenthesedSelect.setSelect(plainSelect);
        existsExpression.setRightExpression(parenthesedSelect);
        return existsExpression;
    }

    /**
     * 取表的别名用于拼接列前缀，无别名时返回表名
     * @param table 目标表
     * @return 别名或表名
     */
    private String parseAliseOrTableName(Table table) {
        return Optional.ofNullable(table).map(Table::getAlias).map(Alias::getName).orElse(table.getName());
    }

    private Column getMainTableColumn(String tableName, String column) {
        return new Column(tableName + "." + column);
    }


    /**
     * 构造 权限表的条件
     * business_function=#{meeting_room} and operate=#{edit} and authority in ( 'd_1','r_1' )
     * <p>
     * TODO 这里把权限串作为字面量拼进语句，没有转义：串里含单引号时会改变语句语义。
     * 当前不可达的前提是 {@code BaseAuth.getAuth()} 只由权限实体的各位置 <b>ID</b> 拼成，
     * 而本框架的业务主键与权限ID都是整型，引号进不来；带名字的 description 不参与拼接。
     * 一旦有外部项目用字符串权限ID接入，这条就是可打通的注入面，届时需要在此做字面量转义
     * （单引号双写），而不是改成 JdbcParameter——改写发生在已渲染的语句文本上，
     * 加占位符会打乱 MyBatis 的 parameterMappings。2026-10-20 裁决本轮不改，只登记债务。
     *
     * @param authRange
     * @return
     */
    private AndExpression authTableWhereExpression(AuthRange authRange) {
        //2 构造 `authority in ('d_1','r_1')` 条件
        //2.1 构造权限字符串列表
        ParenthesedExpressionList<StringValue> authValues = new ParenthesedExpressionList<>(
                authRange.getAuthorities().stream().map(FlatteningAuthority::getAuth).map(StringValue::new).collect(Collectors.toList())
        );
        //2.2 构造表达式
        InExpression authInExpression = new InExpression(authDataTable.getAuthorityColumn(), authValues);

        //3 构造 business_function 条件
        AndExpression inAndBusinessFunctionExpression = new AndExpression(
                authInExpression,
                new EqualsTo(authDataTable.getBusinessFunctionColumn(), new StringValue(authRange.getBusinessFunction()))
        );
        //4 构造 operate 条件
        AndExpression inAndBusinessFunctionAndOperateExpression = new AndExpression(
                inAndBusinessFunctionExpression,
                new EqualsTo(authDataTable.getOperateColumn(), new StringValue(authRange.getOperate()))
        );
        return inAndBusinessFunctionAndOperateExpression;
    }

    /**
     * 保存PlainSelect和对应的Table
     */
    static class PlainSelectAndTable {
        PlainSelect plainSelect;
        Table table;

        public PlainSelectAndTable(PlainSelect plainSelect, Table table) {
            this.plainSelect = plainSelect;
            this.table = table;
        }
    }
}
