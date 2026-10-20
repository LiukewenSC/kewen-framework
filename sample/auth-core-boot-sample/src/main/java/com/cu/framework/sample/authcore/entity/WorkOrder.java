package com.cu.framework.sample.authcore.entity;

import lombok.Data;

import java.util.List;

/**
 * 示例业务表 demo_work_order 的行。
 * <p>
 * 两个客户外键分别指向 demo_customer 的两个别名实例，用于验证 tableAlias 能把权限条件
 * 精确挂到同表的那一个实例上；sameCustomerOrders 由映射文件的嵌套查询填充，用于验证嵌套声明
 *
 * @author kewen
 * @since 2026-10-20
 */
@Data
public class WorkOrder {

    /** 主键，同时是数据权限里的业务数据ID */
    private Long id;
    /** 工单标题 */
    private String title;
    /** 归属客户ID，多表语句中的别名 cu */
    private Long customerId;
    /** 下单客户ID，多表语句中的别名 cb */
    private Long bookingCustomerId;
    /** 金额 */
    private Integer amount;
    /** 状态 */
    private String status;
    /** 同一归属客户下的工单，由被声明的嵌套查询填充 */
    private List<WorkOrder> sameCustomerOrders;
}
