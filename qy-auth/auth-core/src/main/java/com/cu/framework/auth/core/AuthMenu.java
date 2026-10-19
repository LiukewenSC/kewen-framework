package com.cu.framework.auth.core;

import java.lang.annotation.*;

/**
 * 菜单权限管辖注解 与 {@link com.cu.framework.auth.core.menu.MenuAccessInterceptor} 配合使用。
 * 只声明"管不管辖"，不带属性；显示文案由 {@link AuthMenuDoc} 承担。
 * 标在方法上管辖该端点，标在类上管辖该 Controller 的全部端点
 * @author kewen
 * @since 2022-11-23
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.TYPE,ElementType.ANNOTATION_TYPE})
public @interface AuthMenu {
}
