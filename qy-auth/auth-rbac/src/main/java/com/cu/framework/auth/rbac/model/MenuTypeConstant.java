package com.cu.framework.auth.rbac.model;

/**
 * 菜单相关类型常量。以枚举定义语义、以 code 与数据库中的 Integer 列交互
 *
 * @author kewen
 * @since 2023-04-07
 */
public final class MenuTypeConstant {

    private MenuTypeConstant() {
    }

    /**
     * 路由/菜单类型（对应 sys_menu_route.type）
     */
    public enum TYPE {
        /** 菜单 */
        MENU_TYPE_MENU(1),
        /** 按钮 */
        MENU_TYPE_BUTTON(2);

        private final Integer code;

        TYPE(Integer code) {
            this.code = code;
        }

        public Integer getCode() {
            return code;
        }

        public static TYPE of(Integer code) {
            for (TYPE t : values()) {
                if (t.code.equals(code)) {
                    return t;
                }
            }
            return null;
        }
    }

    /**
     * 菜单权限类型（对应 sys_menu_api.auth_type）
     */
    public enum AUTH_TYPE {
        /** 基于自身权限 */
        OWNER(1),
        /** 基于父菜单权限 */
        PARENT(2);

        private final Integer code;

        AUTH_TYPE(Integer code) {
            this.code = code;
        }

        public Integer getCode() {
            return code;
        }

        public static AUTH_TYPE of(Integer code) {
            for (AUTH_TYPE t : values()) {
                if (t.code.equals(code)) {
                    return t;
                }
            }
            return null;
        }
    }
}
