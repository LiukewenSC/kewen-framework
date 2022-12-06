package com.kewen.framework.code.generator;

import java.util.Arrays;
import java.util.List;

/**
 * @descrpition 配置类，代码生成的时候需要配置的信息
 * @author kewen
 * @since 2022-12-05 11:05
 */
public class Config {

    /**
     * 基础包名
     */
    public static final String BASE_PACKAGE = "com.kewen.framework.boot.authority";
    /**
     * 模块名
     */
    public static final String MODULE_NAME = "biz";

    /**
     * 数据库配置文件，resources下的配置文件，主要解析 spring.datasource 下的数据库连接参数
     */
    public static final String APPLICATION_CONFIG_FILE = "application-dev.properties";

    /**
     * 作者
     */
    public static final String AUTHOR = "kewen";
    /**
     * 忽略的表名前缀，模糊前匹配
     */
    public static final List<String> IGNORE_TABLE_PREFIX= Arrays.asList("sys_","log_","yjt_log");


}
