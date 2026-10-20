package com.cu.framework.sample.authcore.trace;

import com.cu.framework.sample.authcore.context.DemoUserContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 验证观察台：按追踪标识记下"这一次请求里到底发生了什么"。
 * <p>
 * 规格里有几条只能靠过程证据判定，光看响应码不够：注入之后的语句长什么样、
 * 未被声明的语句有没有被改写、超出上限时有没有去打库。这些都要有一个可查询的落点，
 * 所以按 {@code X-Trace-Id} 收集，脚本可以用一个独立请求把过程取回来比对。
 * <p>
 * 没带追踪标识的请求不记录，因此正常调用零开销；缓存是定长的，只保留最近的若干次
 *
 * @author kewen
 * @since 2026-10-20
 */
public class DemoSqlTrace {

    /** 保留的追踪标识数量上限 */
    private static final int MAX_TRACES = 60;

    /** 追踪标识 -> 过程记录 */
    private final Map<String, List<Entry>> byTraceId = Collections.synchronizedMap(
            new LinkedHashMap<String, List<Entry>>(16, 0.75f, false) {
                private static final long serialVersionUID = 1L;

                @Override
                protected boolean removeEldestEntry(Map.Entry<String, List<Entry>> eldest) {
                    return size() > MAX_TRACES;
                }
            });

    /**
     * 记录一条实际下发的语句
     *
     * @param source 来源通道，mybatis 或 jdbc
     * @param sql    实际下发到数据库的语句文本
     */
    public void recordSql(String source, String sql) {
        String traceId = DemoUserContext.traceId();
        if (traceId == null) {
            return;
        }
        entries(traceId).add(Entry.sql(source, sql));
    }

    /**
     * 记录一次行级判定被真正调用（用于证明"超出上限时没有去打库"）
     *
     * @param bizModule 业务功能
     * @param action    操作类型
     * @param idCount   本次判定的数据ID数量
     */
    public void recordJudge(String bizModule, String action, int idCount) {
        String traceId = DemoUserContext.traceId();
        if (traceId == null) {
            return;
        }
        entries(traceId).add(Entry.judge(bizModule, action, idCount));
    }

    /**
     * 取某个追踪标识下的全部过程记录
     *
     * @param traceId 追踪标识
     * @return 记录列表，没有记录时为空列表
     */
    public List<Entry> get(String traceId) {
        List<Entry> list = byTraceId.get(traceId);
        return list == null ? Collections.<Entry>emptyList() : new ArrayList<>(list);
    }

    /**
     * 清空全部记录，便于脚本按场景起点计数
     */
    public void clear() {
        byTraceId.clear();
    }

    /**
     * 取（或创建）某追踪标识的记录容器
     *
     * @param traceId 追踪标识
     * @return 记录列表
     */
    private List<Entry> entries(String traceId) {
        synchronized (byTraceId) {
            List<Entry> list = byTraceId.get(traceId);
            if (list == null) {
                list = Collections.synchronizedList(new ArrayList<Entry>());
                byTraceId.put(traceId, list);
            }
            return list;
        }
    }

    /**
     * 一条过程记录
     */
    public static class Entry {
        /** 记录类型：sql 或 judge */
        private final String type;
        /** 来源通道或业务功能 */
        private final String source;
        /** 语句文本 */
        private final String sql;
        /** 操作类型 */
        private final String action;
        /** 数据ID数量 */
        private final Integer idCount;

        private Entry(String type, String source, String sql, String action, Integer idCount) {
            this.type = type;
            this.source = source;
            this.sql = sql;
            this.action = action;
            this.idCount = idCount;
        }

        /**
         * @param source 来源通道
         * @param sql    语句文本
         * @return 语句记录
         */
        static Entry sql(String source, String sql) {
            return new Entry("sql", source, sql, null, null);
        }

        /**
         * @param bizModule 业务功能
         * @param action    操作类型
         * @param idCount   数据ID数量
         * @return 判定记录
         */
        static Entry judge(String bizModule, String action, int idCount) {
            return new Entry("judge", bizModule, null, action, idCount);
        }

        public String getType() {
            return type;
        }

        public String getSource() {
            return source;
        }

        public String getSql() {
            return sql;
        }

        public String getAction() {
            return action;
        }

        public Integer getIdCount() {
            return idCount;
        }
    }
}
