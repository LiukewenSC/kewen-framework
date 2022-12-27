package com.kewen.framework.boot.authority.advance.datacheck;

import java.lang.annotation.*;

/**
 * @descrpition 业务操作权限 加在controller切面
 *  select business_id from sys_application_auth
 *  where application=#{application} and operate=#{operate} and business_id=#{businessId} and authority in ( #{用户权限} )
 *  limit 1
 * businessId需要关联获取 {@link ApplicationBusiness}
 * @author kewen
 * @since 2022-11-23 11:55
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DataCheck {
    /**
     * 模块ID
     * @return 模块ID
     */
    String module() ;

    /**
     * 操作
     *  默认统一的 对应authority_reference表中的 operate字段，
     *  因为数据库不留空，若没有对操作的要求则默认unified
     *  可以为 unified modify update delete 或者其他自定义的操作，传入时需要匹配对应的操作
     * @return 返回操作类型
     */
    String operate() default "unified";
}
