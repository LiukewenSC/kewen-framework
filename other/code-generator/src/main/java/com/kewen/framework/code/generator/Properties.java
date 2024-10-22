package com.kewen.framework.code.generator;


import lombok.Getter;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * @descrpition 配置类，代码生成的时候需要配置的信息
 * @author kewen
 * @since 2022-12-05 11:05
 */
@Getter
public class Properties {

    /**
     * @param args 从这里执行也可以
     * @throws SQLException
     */
    public static void main(String[] args) throws SQLException {
        CodeGenerator.main(args);
    }

    private final String url = "jdbc:mysql://119.6.253.231:16102/emergency-product";
    private final String username = "root";
    private final String password = "Dplbtob@2019";
    /**
     * 工程绝对路径地址，指定到src上面一层;
     * 为空则表示用代码生成器在的路径
     */
    private final String absolutePath ="D:\\Projects\\kewen-framework\\sample\\auth-boot-sample";

    /**
     * 基础包名
     */
    private final String basePackage = "com.kewen.framework.sample.auth";

    private final String orderByDescProperty="createTime";

    /**
     * 是否开启swagger
     */
    private final boolean enableSwagger = false;
    private final List<String> containsTablePrefix = Arrays.asList("sys_user");
    private final List<String> ignoreTablePrefix = Arrays.asList("bak_","sys_log");
    /**
     * 作者
     */
    private final String author = "kewen";


}