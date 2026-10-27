package com.cu.framework.auth.core;

import java.lang.annotation.*;

/**
 * 菜单显示文案注解 与 {@link com.cu.framework.auth.core.menu.MenuApiGeneratorProcessor} 配合使用，
 * 由它生成菜单树的标签；是否受管辖由 {@link AuthMenu} 决定，本注解不参与鉴权判定
 * @author kewen
 * @since 2026-10-20
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.TYPE,ElementType.ANNOTATION_TYPE})
public @interface AuthMenuDoc {

    /**
     * 显示名称。标在类上为分组行命名，标在方法上为端点行命名；
     * 缺失时由生成逻辑回落到类简单名或方法名
     * @return 显示名称
     */
    String name();
}
