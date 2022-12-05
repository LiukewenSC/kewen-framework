package com.kewen.framework.boot.authority.advance.authrange;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @descrpition 查询权限范围注解
 * 在mapper拦截 ，用于数据权限列表查询
 * 例如 拼接语句：
 * select * from ${business_table} where
 *   ${business_table}.{business_id} in
 *      (   select business_id from sys_application_auth
 *          where application=#{application} and operate=#{operate} and authority in ( #{用户权限} )
 *      )
 * 拼接 where后面部分，where前半部分为业务定义的sql，本增强只是在后加上 and 的权限查询语句，避免业务中都需要主动关联权限表匹配，
 * 实现逻辑解耦
 * where后有条件也不用担心，会自动加上and
 * @author kewen
 * @since 2022-11-23 10:52
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AuthRange {

    /**
     * 模块
     * @return
     */
    String module() ;


    /**
     * 表别名，用于拼接  t.id
     * @return
     */
    String tableAlias() ;

    /**
     * 业务主键column名 用于拼接 t.id
     * @return
     */
    String businessColumn() default "id";

    /**
     * 默认统一的
     * @return 返回操作类型
     */
    String operate() default "unified";

    /**
     * 条件匹配方式 in/exists
     * 关联原则 小表驱动大表
     * 默认通过in的方式，当权限表中数据大时应该采用exists方式
     * @return
     */
    MatchMethod matchMethod() default MatchMethod.IN;

}