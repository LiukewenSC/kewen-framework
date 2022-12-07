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
     * 工程绝对路径地址，指定到src上面一层;
     * 为空则表示用代码生成器在的路径
     */
    public static final String ABSOLUTE_PATH="D:\\Projects\\kewen-framework\\cloud\\security-oauth-server";

    /**
     * 基础包名
     */
    public static final String BASE_PACKAGE = "com.kewen.framework.cloud.security";
    /**
     * 模块名
     */
    public static final String MODULE_NAME = "service";

    /**
     * 数据库配置文件，resources下的配置文件，主要解析 spring.datasource 下的数据库连接参数
     */
    public static final String APPLICATION_PROPERTIES = "application-dev.properties";
    /**
     * 是否开启swagger
     */
    public static final boolean ENABLE_SWAGGER = false;


    /**
     * MySQL的schema
     */
    public static final String SCHEMA = "uucs";
    //public static final List<String> IGNORE_TABLE_PREFIX = Arrays.asList("sys_");
    public static final List<String> IGNORE_TABLE_PREFIX = null;
    public static final List<String> CONTAINS_TABLE_PREFIX = Arrays.asList("sys_");

    /**
     * 作者
     */
    public static final String AUTHOR = "kewen";

}
