package com.cu.framework.auth.core.data;

import com.cu.framework.auth.core.entity.BaseAuth;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 数据权限处理器的批量判定测试：权限串在内存中比对，数据ID按字符串归一化，
 * 任一ID未命中即整体拒绝，且无权限时不打库。
 *
 * @author kewen
 * @since 2026-10-20
 */
public class JdbcAuthDataHandlerTest {

    private static final String BUSINESS_FUNCTION = "meeting_room";
    private static final String OPERATE = "approve";

    private StubPersistent persistent;
    private JdbcAuthDataHandler handler;

    @Before
    public void setUp() {
        persistent = new StubPersistent();
        handler = new JdbcAuthDataHandler(persistent);
    }

    /** 只提供内存结果的权限表替身，记录查询与写入次数 */
    static class StubPersistent extends JdbcAuthDataPersistent {
        final List<AuthDataDO> rows = new ArrayList<>();
        final List<Collection<AuthDataDO>> inserted = new ArrayList<>();
        final List<List<Object>> deleted = new ArrayList<>();
        int queryCount;

        StubPersistent() {
            super(new org.springframework.jdbc.core.JdbcTemplate(), new AuthDataTable());
        }

        @Override
        public List<AuthDataDO> listAuthData(String businessFunction, Collection<Object> dataIds, String operate) {
            queryCount++;
            return rows;
        }

        @Override
        public boolean insertBatchAuthData(Collection<AuthDataDO> datas) {
            inserted.add(new ArrayList<>(datas));
            return true;
        }

        @Override
        public int deleteBatchAuthData(List<Object> ids) {
            deleted.add(new ArrayList<>(ids));
            return ids.size();
        }
    }

    private static AuthDataDO row(Object dataId, String authority) {
        return new AuthDataDO(1L, BUSINESS_FUNCTION, dataId, OPERATE, authority, "desc");
    }

    private static Collection<BaseAuth> userAuths(String... authorities) {
        List<BaseAuth> list = new ArrayList<>();
        for (String authority : authorities) {
            list.add(new BaseAuth(authority, "desc"));
        }
        return list;
    }

    @Test
    public void everyDataIdGrantedPasses() {
        persistent.rows.addAll(Arrays.asList(row(1L, "ROLE__1"), row(2L, "ROLE__1")));

        Assert.assertTrue(handler.hasDataOperateAuths(userAuths("ROLE__1"), BUSINESS_FUNCTION,
                Arrays.asList(1L, 2L), OPERATE));
    }

    /**
     * 改造前只判定第一条因而会放行，这里必须整体拒绝
     */
    @Test
    public void oneUngrantedDataIdRejectsAll() {
        persistent.rows.add(row(1L, "ROLE__1"));

        Assert.assertFalse(handler.hasDataOperateAuths(userAuths("ROLE__1"), BUSINESS_FUNCTION,
                Arrays.asList(1L, 2L), OPERATE));
    }

    @Test
    public void authorityNotOwnedByUserDoesNotCount() {
        persistent.rows.add(row(1L, "ROLE__8"));

        Assert.assertFalse(handler.hasDataOperateAuths(userAuths("ROLE__1"), BUSINESS_FUNCTION,
                Collections.singletonList(1L), OPERATE));
    }

    /**
     * 同一主键的不同类型形态必须被视为同一条数据，否则会把有权限的调用误判为拒绝
     */
    @Test
    public void dataIdTypeFormsAreNormalized() {
        persistent.rows.add(row(1L, "ROLE__1"));

        Assert.assertTrue(handler.hasDataOperateAuths(userAuths("ROLE__1"), BUSINESS_FUNCTION,
                Collections.singletonList((Object) Integer.valueOf(1)), OPERATE));
    }

    @Test
    public void stringDataIdIsSupported() {
        persistent.rows.add(row("ROOM-A", "ROLE__1"));

        Assert.assertTrue(handler.hasDataOperateAuths(userAuths("ROLE__1"), BUSINESS_FUNCTION,
                Collections.singletonList((Object) "ROOM-A"), OPERATE));
        Assert.assertFalse(handler.hasDataOperateAuths(userAuths("ROLE__1"), BUSINESS_FUNCTION,
                Collections.singletonList((Object) "ROOM-B"), OPERATE));
    }

    /**
     * 无权限时不打库：既避免未登录态产生查询，也让拒绝原因不会被误读为查询异常
     */
    @Test
    public void emptyUserAuthsRejectsWithoutQuery() {
        persistent.rows.add(row(1L, "ROLE__1"));

        Assert.assertFalse(handler.hasDataOperateAuths(Collections.emptyList(), BUSINESS_FUNCTION,
                Collections.singletonList(1L), OPERATE));
        Assert.assertFalse(handler.hasDataOperateAuths((Collection<BaseAuth>) null, BUSINESS_FUNCTION,
                Collections.singletonList(1L), OPERATE));
        Assert.assertEquals("权限集合为空时不应发起查询", 0, persistent.queryCount);
    }

    @Test
    public void emptyDataIdsRejectsWithoutQuery() {
        Assert.assertFalse(handler.hasDataOperateAuths(userAuths("ROLE__1"), BUSINESS_FUNCTION,
                Collections.emptyList(), OPERATE));
        Assert.assertEquals(0, persistent.queryCount);
    }

    /**
     * 单条数据的权限维护保持原有语义：只查询这一条数据的现有授权，只补差异、不动未变的授权
     */
    @Test
    public void editDataAuthsKeepsSingleRecordSemantics() {
        persistent.rows.add(row(1L, "ROLE__1"));
        BaseAuth keep = new BaseAuth("ROLE__1", "desc");
        BaseAuth added = new BaseAuth("ROLE__2", "desc");

        handler.editDataAuths(BUSINESS_FUNCTION, 1L, OPERATE, Arrays.asList(keep, added));

        Assert.assertEquals("单条数据的权限维护只应查询该条数据", 1, persistent.queryCount);
        Assert.assertEquals(1, persistent.inserted.size());
        Assert.assertEquals("已存在的授权不应重复插入", 1, persistent.inserted.get(0).size());
        Assert.assertEquals(OPERATE, persistent.inserted.get(0).iterator().next().getOperate());
        Assert.assertTrue("没有需要撤下的授权时不应删除", persistent.deleted.isEmpty());
    }
}
