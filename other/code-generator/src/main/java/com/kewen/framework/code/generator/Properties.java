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

    /**
     * 数据库连接URL - 请替换为实际的数据库地址
     * 示例: jdbc:mysql://localhost:3306/your_database
     */
    private final String url = "jdbc:mysql://localhost:3306/your_database";
    
    /**
     * 数据库用户名 - 请替换为实际的用户名
     */
    private final String username = "your_username";
    
    /**
     * 数据库密码 - 请替换为实际的密码
     */
    private final String password = "your_password";
    /**
     * 工程绝对路径地址，指定到src上面一层;
     * 为空则表示用代码生成器在的路径
     * 示例: /path/to/your/project/sample/auth-boot-sample
     */
    private final String absolutePath = "";

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
