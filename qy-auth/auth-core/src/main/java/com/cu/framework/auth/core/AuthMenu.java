package com.cu.framework.auth.core;

import java.lang.annotation.*;

/**
 * 菜单权限路径注解 与 {@link com.cu.framework.auth.core.menu.MenuAccessInterceptor} 配合使用
 * @author kewen
 * @since 2022-11-23
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.TYPE,ElementType.ANNOTATION_TYPE})
public @interface AuthMenu {

    /**
     * 名字，数据库便于识别使用。不参与逻辑
     * @return
     */
    String name();
}
