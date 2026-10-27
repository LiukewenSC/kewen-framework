package com.cu.framework.auth.core.data.edit;

import com.cu.framework.auth.core.AuthCurrentUserContextHolder;
import com.cu.framework.auth.core.AuthDataOperation;
import com.cu.framework.auth.core.data.AuthDataHandler;
import com.cu.framework.auth.core.entity.FlatteningAuthority;
import com.cu.framework.auth.core.entity.CurrentUser;
import com.cu.framework.auth.core.exception.AuthException;
import com.cu.framework.auth.core.exception.AuthServiceException;
import org.aspectj.lang.JoinPoint;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 行级操作权限切面的回归测试：一次调用涉及的每一个业务数据ID都必须被判定，
 * 且"无法判定"与"没有权限"要分成两类失败。
 *
 * @author kewen
 * @since 2026-10-20
 */
public class DataCheckAspectTest {

    private static final String BUSINESS_FUNCTION = "meeting_room";
    private static final String OPERATE = "approve";
    /** 与切面内部上限保持一致 */
    private static final int MAX_DATA_IDS = 100;

    private DataCheckAspect aspect;
    private RecordingHandler handler;

    @Before
    public void setUp() {
        handler = new RecordingHandler();
        aspect = new DataCheckAspect(handler, new FixedContext());
    }

    /** 交出若干业务数据ID的载体 */
    static class Carrier implements DataIdCarrier<Object> {
        private final List<Object> ids;

        Carrier(Object... ids) {
            this.ids = new ArrayList<>(Arrays.asList(ids));
        }

        @Override
        public Collection<Object> getDataIds() {
            return ids;
        }
    }

    /** 记录被询问到的ID，并按 granted 集合判定是否放行 */
    static class RecordingHandler implements AuthDataHandler {
        final List<Collection<Object>> asked = new ArrayList<>();
        final Set<String> granted = new HashSet<>();

        @Override
        public boolean hasDataOperateAuths(Collection<FlatteningAuthority> auths, String businessFunction, Collection<Object> dataIds, String operate) {
            asked.add(new LinkedHashSet<>(dataIds));
            for (Object dataId : dataIds) {
                if (!granted.contains(String.valueOf(dataId))) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public void editDataAuths(String businessFunction, Object dataId, String operate, Collection<FlatteningAuthority> auths) {
        }

        @Override
        public Collection<FlatteningAuthority> getDataAuths(String businessFunction, Object dataId, String operate) {
            return Collections.emptyList();
        }

        @Override
        public void deleteBusinessFunctionAuthByDataId(String businessFunction, Object dataId) {
        }

        Collection<Object> askedOnce() {
            Assert.assertEquals("权限判定应只发起一次批量询问", 1, asked.size());
            return asked.get(0);
        }
    }

    static class FixedContext implements AuthCurrentUserContextHolder {
        @Override
        public CurrentUser getUser() {
            return null;
        }

        @Override
        public Collection<FlatteningAuthority> getAuths() {
            return Collections.singletonList(new FlatteningAuthority("ROLE__1", "ROLE__超级管理员"));
        }
    }

    /** 只提供 getArgs() 与 toString() 的 JoinPoint 替身 */
    private static JoinPoint joinPoint(final Object... args) {
        return (JoinPoint) Proxy.newProxyInstance(JoinPoint.class.getClassLoader(),
                new Class[]{JoinPoint.class},
                (proxy, method, methodArgs) -> {
                    switch (method.getName()) {
                        case "getArgs":
                            return args;
                        case "toString":
                            return "execution(probe)";
                        case "hashCode":
                            return System.identityHashCode(proxy);
                        case "equals":
                            return proxy == methodArgs[0];
                        default:
                            return null;
                    }
                });
    }

    private static AuthDataOperation annotation() throws Exception {
        Method method = DataCheckAspectTest.class.getMethod("annotated");
        return method.getAnnotation(AuthDataOperation.class);
    }

    @AuthDataOperation(businessFunction = BUSINESS_FUNCTION, operate = OPERATE)
    public void annotated() {
    }

    private void grant(Object... ids) {
        for (Object id : ids) {
            handler.granted.add(String.valueOf(id));
        }
    }

    @Test
    public void singleDataIdHitPasses() throws Exception {
        grant(1L);

        aspect.before(joinPoint(new Carrier(1L)), annotation());

        Assert.assertEquals(1, handler.askedOnce().size());
    }

    @Test(expected = AuthException.class)
    public void singleDataIdMissRejects() throws Exception {
        grant(999L);

        aspect.before(joinPoint(new Carrier(1L)), annotation());
    }

    @Test
    public void allDataIdsHitPasses() throws Exception {
        grant(1L, 2L, 3L);

        aspect.before(joinPoint(new Carrier(1L, 2L, 3L)), annotation());

        Assert.assertEquals(3, handler.askedOnce().size());
    }

    /**
     * 改造前这里会因只判定第一个ID而放行，是本用例要守住的修复点
     */
    @Test
    public void partiallyGrantedRejectsWholeCall() throws Exception {
        grant(1L);

        try {
            aspect.before(joinPoint(new Carrier(1L, 2L)), annotation());
            Assert.fail("第二条数据无权限时应整体拒绝");
        } catch (AuthException expected) {
            Assert.assertEquals("权限校验不通过", expected.getMessage());
        }
        Assert.assertEquals(2, handler.askedOnce().size());
    }

    @Test
    public void carriersSpreadOverArgumentsAreAllCollected() throws Exception {
        grant(1L, 2L);

        aspect.before(joinPoint(new Carrier(1L), new Carrier(2L)), annotation());

        Assert.assertEquals(2, handler.askedOnce().size());
    }

    @Test
    public void carriersInsideCollectionAndArrayAreAllCollected() throws Exception {
        grant(1L, 2L, 3L);

        aspect.before(joinPoint(Arrays.asList(new Carrier(1L), new Carrier(2L)),
                new Object[]{new Carrier(3L)}), annotation());

        Assert.assertEquals(3, handler.askedOnce().size());
    }

    @Test
    public void duplicatedDataIdsAreJudgedOnce() throws Exception {
        grant(1L, 2L);

        aspect.before(joinPoint(new Carrier(1L, 1L, 2L)), annotation());

        Assert.assertEquals(2, handler.askedOnce().size());
    }

    @Test(expected = AuthServiceException.class)
    public void missingCarrierIsUsageError() throws Exception {
        aspect.before(joinPoint("普通参数", 1L), annotation());
    }

    @Test(expected = AuthServiceException.class)
    public void emptyDataIdsIsUsageError() throws Exception {
        aspect.before(joinPoint(new Carrier()), annotation());
    }

    @Test(expected = AuthServiceException.class)
    public void nullDataIdIsUsageError() throws Exception {
        aspect.before(joinPoint(new Carrier(1L, null)), annotation());
    }

    /**
     * 上限之内正常判定，且去重后恰好100条不触发失败
     */
    @Test
    public void upToLimitIsJudged() throws Exception {
        Object[] ids = new Object[MAX_DATA_IDS];
        for (int i = 0; i < MAX_DATA_IDS; i++) {
            ids[i] = (Object) (long) i;
            grant(ids[i]);
        }

        aspect.before(joinPoint(new Carrier(ids)), annotation());

        Assert.assertEquals(MAX_DATA_IDS, handler.askedOnce().size());
    }

    @Test
    public void overLimitFailsWithoutAnyQuery() throws Exception {
        Object[] ids = new Object[MAX_DATA_IDS + 1];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = (Object) (long) i;
        }

        try {
            aspect.before(joinPoint(new Carrier(ids)), annotation());
            Assert.fail("超过上限应直接失败");
        } catch (AuthServiceException expected) {
            Assert.assertTrue("失败信息应说明上限，实际为：" + expected.getMessage(),
                    expected.getMessage().contains(String.valueOf(MAX_DATA_IDS)));
        }
        Assert.assertTrue("超出上限时不应发起权限查询", handler.asked.isEmpty());
    }

    /**
     * 对外消息不得带出框架内部类型名
     */
    @Test
    public void usageErrorMessageDoesNotLeakInternalTypeName() throws Exception {
        try {
            aspect.before(joinPoint("没有承载对象"), annotation());
            Assert.fail("应抛用法错误");
        } catch (AuthServiceException expected) {
            Assert.assertFalse("消息不得包含承载接口名：" + expected.getMessage(),
                    expected.getMessage().contains("DataIdCarrier"));
            Assert.assertFalse("消息不得包含异常类名：" + expected.getMessage(),
                    expected.getMessage().contains("AuthServiceException"));
        }
    }
}
