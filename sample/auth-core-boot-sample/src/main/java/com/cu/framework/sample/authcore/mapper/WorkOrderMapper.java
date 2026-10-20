package com.cu.framework.sample.authcore.mapper;

import com.cu.framework.auth.core.AuthDataRange;
import com.cu.framework.auth.core.data.range.MatchMethod;
import com.cu.framework.sample.authcore.entity.WorkOrder;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 工单查询，数据范围声明的主战场。
 * <p>
 * 每个 {@link AuthDataRange} 都标在本接口的查询方法上：标在哪个方法，权限条件就只注入那一条语句。
 * 同一张表要按多个操作过滤时，各操作一个方法，槽位在方法级别就是固定的
 *
 * @author kewen
 * @since 2026-10-20
 */
public interface WorkOrderMapper {

    /**
     * 可列表（unified 槽位，IN 匹配）。alice 命中 1/2/4，bob 命中 1，无权限时零行
     *
     * @return 当前用户可见的工单
     */
    @AuthDataRange(businessFunction = "work_order", operate = "unified")
    List<WorkOrder> listRangeIn();

    /**
     * 与 {@link #listRangeIn()} 同一槽位、同一行集，只把匹配方式换成 EXISTS
     *
     * @return 当前用户可见的工单
     */
    @AuthDataRange(businessFunction = "work_order", operate = "unified", matchMethod = MatchMethod.EXISTS)
    List<WorkOrder> listRangeExists();

    /**
     * 与列表同一槽位的计数，验证注入同样作用于聚合查询
     *
     * @return 当前用户可见的行数
     */
    @AuthDataRange(businessFunction = "work_order", operate = "unified")
    int countRange();

    /**
     * 可编辑列表：同表、另一个操作槽位
     *
     * @return 命中标注编辑权限的工单
     */
    @AuthDataRange(businessFunction = "work_order", operate = "edit")
    List<WorkOrder> listEditable();

    /**
     * 可审批列表：同表、又一个操作槽位
     *
     * @return 命中标注审批权限的工单
     */
    @AuthDataRange(businessFunction = "work_order", operate = "approve")
    List<WorkOrder> listApprovable();

    /**
     * 全量列表，<b>没有</b>任何声明，作为"未声明即不过滤"的对照
     *
     * @return 所有工单
     */
    List<WorkOrder> listAll();

    /**
     * 外层声明语句：结果映射里用嵌套查询填充同客户的工单，那条内层语句自己也带声明。
     * 用于验证两条语句各按自己的声明注入，外层不因内层结束而失去过滤
     *
     * @return 当前用户可见的工单，并带出其同客户工单
     */
    @AuthDataRange(businessFunction = "work_order", operate = "unified")
    List<WorkOrder> listNested();

    /**
     * 嵌套查询的内层语句，按 edit 槽位过滤，与外层的 unified 槽位不同
     *
     * @param customerId 归属客户ID
     * @return 该客户下当前用户可编辑的工单
     */
    @AuthDataRange(businessFunction = "work_order", operate = "edit")
    List<WorkOrder> listSameCustomer(@Param("customerId") Long customerId);

    /**
     * 多表语句，权限条件挂到别名 cu（归属客户）
     *
     * @return 挂 cu 时可见的工单
     */
    @AuthDataRange(businessFunction = "customer_link", operate = "unified",
            table = "demo_customer", tableAlias = "cu")
    List<WorkOrder> listJoinCu();

    /**
     * 同一条多表语句形态，权限条件挂到别名 cb（下单客户）。
     * 两张关联表是同一张 demo_customer，只有别名能把两者区分开
     *
     * @return 挂 cb 时可见的工单
     */
    @AuthDataRange(businessFunction = "customer_link", operate = "unified",
            table = "demo_customer", tableAlias = "cb")
    List<WorkOrder> listJoinCb();

    /**
     * 动态语句：status 为空时不拼该条件。SQL 只能运行期拼出，启动阶段跳过目标表校验
     *
     * @param status 状态过滤条件，可为 null
     * @return 当前用户可见的工单
     */
    @AuthDataRange(businessFunction = "work_order", operate = "unified")
    List<WorkOrder> listDynamic(@Param("status") String status);

    /**
     * 动态语句的失败面：withJoin 为 false 时运行期拼不出声明的 demo_customer，
     * 必须在执行期抛异常，而不是返回未经过滤的结果
     *
     * @param withJoin 是否拼出关联表
     * @return 查询结果（正常调用拿不到，因为会抛异常）
     */
    @AuthDataRange(businessFunction = "customer_link", operate = "unified", table = "demo_customer")
    List<WorkOrder> listDynamicMissingTable(@Param("withJoin") boolean withJoin);

    /**
     * 写语句，未声明也声明不了（数据范围只作用于查询）。用于验证写语句原文下发
     *
     * @param id     工单ID
     * @param status 新状态
     * @return 更新条数
     */
    int updateStatus(@Param("id") Long id, @Param("status") String status);
}
