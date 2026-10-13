package com.cu.framework.basic.logger.trace;

import com.cu.framework.basic.logger.LoggerConstant;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.MDC;

/**
 * @descrpition TraceContext 的写入与清除契约
 * @author kewen
 * @since 2026-10-13
 */
public class TraceContextTest {

    @After
    public void tearDown() {
        MDC.clear();
    }

    /**
     * 前置校验：MDC 必须在当前测试运行环境下真实可用，否则下面所有断言都会因为
     * slf4j 无 binding 而退化成 no-op，得到假阳性通过。
     */
    @Test
    public void mdcAdapterIsFunctional() {
        MDC.put("probe", "probe-value");
        Assert.assertEquals("slf4j 无可用 MDCAdapter，需为 basic 模块添加 test scope 的日志实现",
                "probe-value", MDC.get("probe"));
        MDC.remove("probe");
    }

    @Test
    public void removeClearsTheFixedKey() {
        TraceContext.set("20261013000001");
        Assert.assertEquals("20261013000001", MDC.get(LoggerConstant.TRACE_ID_KEY));

        TraceContext.remove();

        Assert.assertNull(MDC.get(LoggerConstant.TRACE_ID_KEY));
    }

    /**
     * 回归：旧实现把流水号的值当作 MDC 的 key 传入 remove，导致流水号值不等于 "traceId"
     * 时键永不清除。
     */
    @Test
    public void removeDoesNotDependOnTheTraceIdValue() {
        String traceIdValue = "20261013000002";
        Assert.assertNotEquals("用例前提：流水号的值不应恰好等于键名",
                LoggerConstant.TRACE_ID_KEY, traceIdValue);

        TraceContext.set(traceIdValue);
        TraceContext.remove();

        Assert.assertNull("固定键必须被清除", MDC.get(LoggerConstant.TRACE_ID_KEY));
        Assert.assertNull("不得以流水号的值作为键写入 MDC", MDC.get(traceIdValue));
    }

    /**
     * 流水号的值恰好等于键名时，清除的仍是键本身，行为与其他取值一致。
     */
    @Test
    public void removeWorksWhenValueEqualsKey() {
        TraceContext.set(LoggerConstant.TRACE_ID_KEY);
        TraceContext.remove();

        Assert.assertNull(MDC.get(LoggerConstant.TRACE_ID_KEY));
    }

    /**
     * 线程复用场景：同一线程 set/remove 成对执行后，后续请求读不到上一个请求的流水号。
     */
    @Test
    public void reusedThreadDoesNotInheritPreviousTraceId() {
        TraceContext.set("request-A");
        Assert.assertEquals("request-A", TraceContext.get());
        TraceContext.remove();

        Assert.assertNull("第二个请求进入时不应残留上一个请求的流水号", TraceContext.get());
    }
}
