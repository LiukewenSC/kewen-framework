package com.kewen.framework.auth.core.annotation.datarange;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Collection;

/**
 * @descrpition 权限范围上下文，用于
 * @author kewen
 * @since 2022-11-23 17:10
 */
public class DataRangeContext {

    private static ThreadLocal<AuthRange> local = new ThreadLocal<>();

    public static AuthRange get(){
        return local.get();
    }
    public static void set(AuthRange auths){
        local.set(auths);
    }
    public static void clear(){
        local.remove();
    }

    @Data
    @Accessors(chain = true)
    public static class AuthRange {
        /**
         * 应用/模块标识
         */
        private String module;
        /**
         * 操作 unified  modify update 等
         */
        private String operate;
        /**
         * 表别名
         */
        private String tableAlias;
        /**
         * 业务主键名 一般为 id
         */
        private String businessColumn;

        /**
         * 用户权限字符串
         */
        private Collection<String> authorities;
    }

}
