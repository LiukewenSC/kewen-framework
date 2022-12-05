package com.kewen.framework.code.generator;

/**
 * @descrpition 配置类，代码生成的时候需要配置的信息
 * @author kewen
 * @since 2022-12-05 11:05
 */
public class Config {

    /**
     * 基础包名
     */
    public static final String BASE_PACKAGE = "com.kewen.framework.code.generator";
    /**
     * 模块名
     */
    public static final String MODULE_NAME = "web";

    /**
     * 数据库配置文件，resources下的配置文件，主要解析 spring.datasource 下的数据库连接参数
     */
    public static final String APPLICATION_PROPERTIES = "application-dev.properties";

    /**
     * 作者
     */
    public static final String AUTHOR = "system";

}
