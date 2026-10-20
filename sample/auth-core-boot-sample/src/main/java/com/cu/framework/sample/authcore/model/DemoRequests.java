package com.cu.framework.sample.authcore.model;

import com.cu.framework.auth.core.data.edit.DataIdCarrier;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 示例的请求体集合。
 * <p>
 * 每个都实现 {@link DataIdCarrier}，把本次调用涉及的<b>全部</b>业务数据ID交出去——
 * 判定单位是ID而不是调用，所以单值场景交出一个元素的集合，批量场景交出整批。
 * 各种交出方式（单值、集合、空集合、含 null、整型短形态、原样字符串）正是行级判定要覆盖的形态
 *
 * @author kewen
 * @since 2026-10-20
 */
public final class DemoRequests {

    private DemoRequests() {
    }

    /**
     * 单条工单的写请求：交出一个ID
     */
    @Data
    public static class OrderEditReq implements DataIdCarrier<Long> {
        /** 工单ID */
        private Long id;
        /** 新标题 */
        private String title;

        @Override
        public Collection<Long> getDataIds() {
            return id == null ? Collections.<Long>emptyList() : Collections.singletonList(id);
        }
    }

    /**
     * 批量工单写请求：一次交出整批ID
     */
    @Data
    public static class OrderBatchReq implements DataIdCarrier<Long> {
        /** 本批工单ID */
        private List<Long> ids = new ArrayList<>();

        @Override
        public Collection<Long> getDataIds() {
            return ids;
        }
    }

    /**
     * 单条资产写请求，交出 Long 形态
     */
    @Data
    public static class AssetEditReq implements DataIdCarrier<Long> {
        /** 资产ID */
        private Long assetId;
        /** 新名称 */
        private String name;

        @Override
        public Collection<Long> getDataIds() {
            return assetId == null ? Collections.<Long>emptyList() : Collections.singletonList(assetId);
        }
    }

    /**
     * 同一条资产写请求，但把主键装成 Integer 形态：
     * 权限表里存的是 BIGINT，读回是 Long，判定要按字符串归一化才不会误判
     */
    @Data
    public static class AssetEditShortFormReq implements DataIdCarrier<Integer> {
        /** 资产ID，故意用短整型形态提交 */
        private Integer assetId;

        @Override
        public Collection<Integer> getDataIds() {
            return assetId == null ? Collections.<Integer>emptyList() : Collections.singletonList(assetId);
        }
    }

    /**
     * 交出空集合：属于用法错误，不是"没有要判定的数据所以放行"
     */
    @Data
    public static class EmptyIdsReq implements DataIdCarrier<Long> {
        /** 恒空的ID集合 */
        private List<Long> ids = new ArrayList<>();

        @Override
        public Collection<Long> getDataIds() {
            return ids;
        }
    }

    /**
     * 交出含 null 的集合：同样属于用法错误
     */
    @Data
    public static class NullIdReq implements DataIdCarrier<Long> {
        /** 含空值的ID集合 */
        private List<Long> ids = new ArrayList<>();

        @Override
        public Collection<Long> getDataIds() {
            return ids;
        }
    }

    /**
     * 一次交出 N 个ID，用来压上限：100 正常判定，101 直接失败且不打库
     */
    @Data
    public static class BulkIdsReq implements DataIdCarrier<Long> {
        /** 本批ID */
        private List<Long> ids = new ArrayList<>();

        @Override
        public Collection<Long> getDataIds() {
            return ids;
        }
    }

    /**
     * 分散承载：一个直接ID、一个集合、一个数组，三类都要被收集到
     */
    @Data
    public static class ScatteredReq {
        /** 单个ID载体 */
        private AssetEditReq primary;
        /** 集合元素载体 */
        private List<AssetEditReq> many = new ArrayList<>();
        /** 数组元素载体 */
        private List<AssetEditReq> more = new ArrayList<>();

        /**
         * 把 more 摊成数组，配合一个集合与一个散参，构造成"ID分散在多个实参与集合、数组元素中"
         *
         * @return 数组形态的载体
         */
        public AssetEditReq[] moreArray() {
            return more.toArray(new AssetEditReq[0]);
        }
    }

    /**
     * 原样交出字符串ID：用来验证载荷里含引号时按整值比对，不会因为被拆成语句片段而匹配到别的行
     */
    @Data
    public static class RawIdReq implements DataIdCarrier<Object> {
        /** 业务数据ID的原始文本，可以含引号与查询语法字符 */
        private String rawId;

        @Override
        public Collection<Object> getDataIds() {
            return rawId == null ? Collections.emptyList() : Collections.<Object>singletonList(rawId);
        }
    }

    /**
     * 管理动作自身的载体：权限维护接口靠它接受行级判定，与被维护的目标数据分开
     */
    @Data
    public static class AdminTargetReq implements DataIdCarrier<Long> {
        /** 管理槽位上的固定数据ID */
        private Long adminId;

        @Override
        public Collection<Long> getDataIds() {
            return adminId == null ? Collections.<Long>emptyList() : Collections.singletonList(adminId);
        }
    }

    /**
     * 授权请求：把某条目标数据的某个槽位设成给定的权限集合（整槽替换，与框架的 diff 语义一致）。
     * <p>
     * 它同时是管理动作自己的承载对象：交出的是被管辖的<b>管理槽位</b>数据ID，
     * 而不是被维护的目标数据ID——框架注释里那条警告说的就是这里，
     * 维护权限的权限必须与目标数据自己的权限分开声明
     */
    @Data
    public static class GrantReq implements DataIdCarrier<Long> {
        /** 业务功能 */
        private String bizModule;
        /** 业务数据ID */
        private Long bizDataId;
        /** 操作类型 */
        private String action;
        /** 权限串 */
        private String authStr;
        /** 权限描述，可含引号与注释符号，用于验证只作为数据存储 */
        private String authDesc;
        /** 管理槽位的数据ID */
        private Long adminId;

        @Override
        public Collection<Long> getDataIds() {
            return adminId == null ? Collections.<Long>emptyList() : Collections.singletonList(adminId);
        }
    }

    /**
     * 清空请求：按业务功能与数据ID清除该数据的全部权限，同样交出管理槽位的ID接受判定
     */
    @Data
    public static class RemoveReq implements DataIdCarrier<Long> {
        /** 业务功能 */
        private String bizModule;
        /** 业务数据ID */
        private Long bizDataId;
        /** 管理槽位的数据ID */
        private Long adminId;

        @Override
        public Collection<Long> getDataIds() {
            return adminId == null ? Collections.<Long>emptyList() : Collections.singletonList(adminId);
        }
    }
}
