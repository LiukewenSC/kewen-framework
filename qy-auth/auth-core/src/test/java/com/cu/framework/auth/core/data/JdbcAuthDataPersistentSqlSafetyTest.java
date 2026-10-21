package com.cu.framework.auth.core.data;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 权限数据读写的 SQL 安全性测试：调用方可控的值必须作为参数传递，不得成为 SQL 文本的一部分。
 * <p>
 * 这里刻意不连任何数据库，只用记录型 JdbcTemplate 捕获"语句文本 + 参数"，
 * 因为要断言的是值有没有被拼进语句，与方言无关。
 *
 * @author kewen
 * @since 2026-10-20
 */
public class JdbcAuthDataPersistentSqlSafetyTest {

    /** 模拟权限表结构，列名与框架 DDL 一致 */
    private static final AuthDataTable TABLE = new AuthDataTable(
            "sys_auth_data", "id", "business_function", "data_id", "operate", "authority", "description");

    /** 载荷中含有引号与括号，旧实现会把它原样拼进语句并撑出额外 VALUES 元组 */
    private static final String PAYLOAD = "ROLE_1'), ('pwn', '999', 'op', 'ADMIN', 'x'), ('zzz";

    private RecordingJdbcTemplate jdbcTemplate;
    private JdbcAuthDataPersistent persistent;

    @Before
    public void setUp() {
        jdbcTemplate = new RecordingJdbcTemplate();
        persistent = new JdbcAuthDataPersistent(jdbcTemplate, TABLE);
    }

    @Test
    public void insertCarriesAuthorityAndDescriptionAsParameters() {
        AuthDataDO data = new AuthDataDO("meeting_room", 1L, "approve", PAYLOAD, "d' -- ");

        persistent.insertBatchAuthData(Collections.singletonList(data));

        Assert.assertEquals(1, jdbcTemplate.sqls.size());
        String sql = jdbcTemplate.sqls.get(0);
        Assert.assertTrue("插入语句必须使用占位符，实际为：" + sql, sql.contains("?"));
        Assert.assertFalse("权限标识不得出现在语句文本中：" + sql, sql.contains(PAYLOAD));
        Assert.assertFalse("权限描述不得出现在语句文本中：" + sql, sql.contains("d' -- "));
        Assert.assertTrue("权限标识必须作为参数下发", jdbcTemplate.allArgs().contains(PAYLOAD));
        Assert.assertTrue("权限描述必须作为参数下发", jdbcTemplate.allArgs().contains("d' -- "));
    }

    @Test
    public void deleteByBusinessFunctionCarriesDataIdAsParameter() {
        persistent.deleteBusinessFunctionDataIdAuth("meeting_room", "1' OR '1'='1");

        Assert.assertEquals(1, jdbcTemplate.sqls.size());
        String sql = jdbcTemplate.sqls.get(0);
        Assert.assertTrue("删除语句必须使用占位符，实际为：" + sql, sql.contains("?"));
        Assert.assertFalse("业务ID不得出现在语句文本中：" + sql, sql.contains("1' OR '1'='1"));
        Assert.assertTrue("业务ID必须作为参数下发", jdbcTemplate.allArgs().contains("1' OR '1'='1"));
    }

    @Test
    public void listCarriesDataIdsAsParameters() {
        persistent.listAuthData("meeting_room", Collections.singletonList((Object) "1' OR '1'='1"), "approve");

        Assert.assertEquals(1, jdbcTemplate.sqls.size());
        String sql = jdbcTemplate.sqls.get(0);
        Assert.assertFalse("业务ID不得出现在语句文本中：" + sql, sql.contains("1' OR '1'='1"));
        Assert.assertTrue("业务ID必须作为参数下发", jdbcTemplate.allArgs().contains("1' OR '1'='1"));
    }

    /**
     * 每个数据ID一个占位符，一条不多一条不少
     */
    @Test
    public void listUsesExactlyOnePlaceholderPerDataId() {
        List<Object> single = Collections.singletonList((Object) 1L);
        persistent.listAuthData("meeting_room", single, "approve");
        Assert.assertEquals(3, countPlaceholders(jdbcTemplate.sqls.get(0)));

        jdbcTemplate.sqls.clear();
        jdbcTemplate.argsList.clear();
        List<Object> hundred = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            hundred.add((Object) i);
        }
        persistent.listAuthData("meeting_room", hundred, "approve");

        String sql = jdbcTemplate.sqls.get(0);
        Assert.assertEquals("业务功能与操作各一个占位符，加上100个数据ID", 102, countPlaceholders(sql));
        Assert.assertEquals(102, jdbcTemplate.allArgs().size());
    }

    @Test
    public void deleteByIdsCarriesIdsAsParameters() {
        persistent.deleteBatchAuthData(Collections.singletonList((Object) "7' OR '1'='1"));

        String sql = jdbcTemplate.sqls.get(0);
        Assert.assertFalse("主键值不得出现在语句文本中：" + sql, sql.contains("7' OR '1'='1"));
        Assert.assertTrue("主键值必须作为参数下发", jdbcTemplate.allArgs().contains("7' OR '1'='1"));
    }

    /**
     * 主键按读回的类型原样绑定，不再按业务ID的类型去猜测，因此字符串主键业务的权限维护不会再撞类型分支
     */
    @Test
    public void deleteByIdsBindsReadBackValuesAsIs() {
        persistent.deleteBatchAuthData(java.util.Arrays.asList((Object) Long.valueOf(7L), (Object) "8"));

        String sql = jdbcTemplate.sqls.get(0);
        Assert.assertEquals("两个主键应各有一个占位符：" + sql, 2, countPlaceholders(sql));
        Assert.assertTrue(jdbcTemplate.allArgs().contains(7L));
        Assert.assertTrue(jdbcTemplate.allArgs().contains("8"));
    }

    private static int countPlaceholders(String sql) {
        return sql.split("\\?", -1).length - 1;
    }

    private static List<Object> flatten(List<Object[]> source) {
        List<Object> all = new ArrayList<>();
        for (Object[] args : source) {
            Collections.addAll(all, args);
        }
        return all;
    }

    /** 记录语句与参数的 JdbcTemplate 替身，不建立任何连接 */
    private static class RecordingJdbcTemplate extends JdbcTemplate {

        private final List<String> sqls = new ArrayList<>();
        private final List<Object[]> argsList = new ArrayList<>();

        @Override
        public int update(String sql) {
            sqls.add(sql);
            argsList.add(new Object[0]);
            return 1;
        }

        @Override
        public int update(String sql, Object... paramValues) {
            sqls.add(sql);
            argsList.add(paramValues);
            return 1;
        }

        @Override
        public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
            sqls.add(sql);
            argsList.add(new Object[0]);
            return new ArrayList<>();
        }

        @Override
        public <T> List<T> query(String sql, Object[] paramValues, RowMapper<T> rowMapper) {
            sqls.add(sql);
            argsList.add(paramValues == null ? new Object[0] : paramValues);
            return new ArrayList<>();
        }

        private List<Object> allArgs() {
            return flatten(argsList);
        }
    }
}
