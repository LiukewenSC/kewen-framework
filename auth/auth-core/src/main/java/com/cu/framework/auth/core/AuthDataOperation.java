package com.cu.framework.auth.core;


import com.cu.framework.auth.core.data.edit.DataIdCarrier;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @descrpition 校验是否有编辑单条或若干条数据的权限
 * <p>
 * 声明位置是 <b>业务方法</b>（通常是 Controller 或 Service 的写方法）：方法执行前，切面从实参中收集所有
 * {@link DataIdCarrier} 交出的业务数据ID（含一层数组与集合元素），逐个判定权限，<b>全部命中才放行</b>，
 * 任一条无权限即整体拒绝，方法体不执行。
 * <p>
 * 判定按下面这条查询取回候选数据的授权记录，再与当前登录人的权限集合在内存中比对：
 * <pre>
 *  select data_id, authority from sys_auth_data
 *  where business_function=#{businessFunction} and operate=#{operate} and data_id in ( #{本次涉及的数据ID} )
 * </pre>
 * 登录人的权限串不进查询条件，避免其规模影响语句本身。数据ID的类型不限（整型、长整型、字符串均可），
 * 比对时按字符串归一化，同一主键的不同类型形态视为同一条数据。
 * <p>
 * 一次调用涉及的数据ID去重后最多 100 条，超出直接失败，由调用方分批提交。
 * 失败分两类：实参中没有承载对象、ID集合为空、含空值、超出上限属于用法错误；
 * 权限未命中属于权限拒绝，对外只给统一的拒绝语义，未命中的明细只进日志。
 * @author kewen
 * @since 2022-11-23
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AuthDataOperation {
    /**
     * 业务功能
     * @return
     */
    String businessFunction() ;

    /**
     * 操作
     *  默认统一的 对应authority_reference表中的 operate字段，
     *  因为数据库不留空，若没有对操作的要求则默认unified
     *  可以为 unified modify update delete 或者其他自定义的操作，传入时需要匹配对应的操作
     * @return 返回操作类型
     */
    String operate() default "unified";
}
