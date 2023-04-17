package com.kewen.framework.auth.core.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @descrpition 菜单权限 加在controller切面
 * @author kewen
 * @since 2022-11-23 11:29
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CheckEndpoint {

    /**
     * 路径，为空则以输入Controller的RequestMapping为准
     * @return
     */
    @AliasFor("value")
    String url() default "";

    @AliasFor("url")
    String value() default "";
}
