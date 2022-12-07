package com.kewen.framework.code.generator;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * @author kewen
 * @descrpition
 * @since 2022-12-05 10:55
 */
public class CodeGenerator {



    public static void main(String[] args) throws SQLException {

        //获取路径
        // /D:/MyProjects/chinaunicom-authorities/target/classes/com/chinaunicom/
        String path = CodeGenerator.class.getResource("").getPath();
        //classpath路径
        String classpath = path.substring(0, path.indexOf("classes") + "classes".length());
        //工程路径
        String projectPath ;
        if (StringUtils.isNotBlank(Config.ABSOLUTE_PATH)){
            projectPath = Config.ABSOLUTE_PATH;
        } else {
            projectPath = path.substring(0, path.indexOf("/target/classes"));
        }


        //数据源配置
        DataSourceConfig.Builder dataSourceConfigBuilder = getDateSourceConfigBuilder(classpath);

        List<String> tableNames = getTableNames(dataSourceConfigBuilder);

        //创建配置
        FastAutoGenerator autoGenerator = createAutoGenerator(dataSourceConfigBuilder, projectPath,tableNames);
        //生成文件
        autoGenerator.execute();
    }

    private static FastAutoGenerator createAutoGenerator(DataSourceConfig.Builder dataSourceConfigBuilder, String projectPath,List<String> tableNames) {
        return FastAutoGenerator.create(dataSourceConfigBuilder)
                .globalConfig(builder -> {
                    builder.author(Config.AUTHOR)
                            .disableOpenDir()
                            .commentDate(() -> LocalDateTime.now().format(DateTimeFormatter.ISO_DATE))
                            .outputDir(projectPath + "/src/main/java")
                            .dateType(DateType.TIME_PACK)
                    ;
                    if (Config.ENABLE_SWAGGER){
                        builder.enableSwagger();
                    }
                }).packageConfig(builder -> {
                    //包配置
                    builder.parent(Config.BASE_PACKAGE)
                            .moduleName(Config.MODULE_NAME)
                            .controller("controller")
                            .service("service")
                            .serviceImpl("service.impl")
                            //.mapper("mapper")
                            .entity("entity")
                            .pathInfo(Collections.singletonMap(OutputFile.xml, projectPath + "/src/main/resources/mapper/" + Config.MODULE_NAME))
                    //.other("output")
                    ;

                }).strategyConfig(builder -> {
                    builder.enableCapitalMode()//开启全局大写命名
                            //.likeTable()模糊表匹配
                            .addInclude()//添加表匹配，指定要生成的数据表名，不写默认选定数据库所有表
                            //.disableSqlFilter()禁用sql过滤:默认(不使用该方法）true
                            //.enableSchema()启用schema:默认false
                            .addInclude(tableNames)
                            .entityBuilder() //实体策略配置
                                //.disableSerialVersionUID()禁用生成SerialVersionUID：默认true
                                .fileOverride()
                                .enableChainModel()//开启链式模型
                                .enableActiveRecord() //开启 ActiveRecord 模式
                                .enableLombok()//开启lombok
                                //.enableRemoveIsPrefix()//开启 Boolean 类型字段移除 is 前缀
                                .enableTableFieldAnnotation()//开启生成实体时生成字段注解
                                //.addTableFills()添加表字段填充
                                .naming(NamingStrategy.underline_to_camel)//数据表映射实体命名策略：默认下划线转驼峰underline_to_camel
                                .columnNaming(NamingStrategy.underline_to_camel)//表字段映射实体属性命名规则：默认null，不指定按照naming执行
                                .idType(IdType.AUTO)//添加全局主键类型
                                //.formatFileName("%s")//格式化实体名称，%s取消首字母I
                                .build()
                            .mapperBuilder()//mapper文件策略
                                .fileOverride()
                                //.enableMapperAnnotation()//开启mapper注解
                                .enableBaseResultMap()//启用xml文件中的BaseResultMap 生成
                                .enableBaseColumnList()//启用xml文件中的BaseColumnList
                                //.cache(缓存类.class)设置缓存实现类
                                //.formatMapperFileName("%sDao")//格式化Dao类名称
                                //.formatXmlFileName("%sXml")//格式化xml文件名称
                                .build()
                            .serviceBuilder()//service文件策略
                                .fileOverride()
                                .formatServiceFileName("%sService")//格式化 service 接口文件名称
                                .formatServiceImplFileName("%sServiceImpl")//格式化 service 接口文件名称
                                .build()
                            .controllerBuilder()//控制层策略
                                .fileOverride()
                                //.enableHyphenStyle()开启驼峰转连字符，默认：false
                                .enableRestStyle()//开启生成@RestController
                                .formatFileName("%sController")//格式化文件名称
                    ;

                })
                .templateConfig(builder -> {
                    //模板配置
                    builder.controller("/templates/controller.java.vm")
                            .service("/templates/service.java.vm")
                            .serviceImpl("/templates/serviceimpl.java.vm")
                            .entity("/templates/entity.java.vm");

                })
                .templateEngine(new VelocityTemplateEngine())
                /*.injectionConfig(builder -> {
                    builder.customMap(Collections.singletonMap("enableSwagger",Config.ENABLE_SWAGGER));
                })*/
                ;
    }

    /**
     * 获取数据库连接配置
     *
     * @param classpath
     * @return
     */
    private static DataSourceConfig.Builder getDateSourceConfigBuilder(String classpath) {
        try {

            Properties pro = new Properties();
            pro.load(new FileInputStream(classpath + "/" + Config.APPLICATION_PROPERTIES));
            String url = pro.getProperty("spring.datasource.url").trim();
            String username = pro.getProperty("spring.datasource.username").trim();
            String password = pro.getProperty("spring.datasource.password").trim();
            return new DataSourceConfig.Builder(url, username, password);
        } catch (Exception e) {
            e.getMessage();
            throw new RuntimeException(e);
        }
    }


    /**
     * 获取Schema下所有表
     *
     * @param dataSourceConfigBuilder
     * @return
     * @throws SQLException
     */
    private static List<String> getTableNames(DataSourceConfig.Builder dataSourceConfigBuilder) throws SQLException {
        String tableSchema = Config.SCHEMA;
        List<String> tableNames = new ArrayList<>();
        Connection connect = dataSourceConfigBuilder.build().getConn();
        Statement stmt = connect.createStatement();
        ResultSet rs = stmt.executeQuery("select table_name from information_schema.tables where table_schema='" + tableSchema + "'");
        while (rs.next()) {
            tableNames.add(rs.getString("TABLE_NAME"));
        }
        //匹配需要保留的
        if (!CollectionUtils.isEmpty(Config.CONTAINS_TABLE_PREFIX)){
            tableNames.removeIf(tableName -> Config.CONTAINS_TABLE_PREFIX.stream().noneMatch(tableName::startsWith));
        }
        //排除在排除列表的
        if (!CollectionUtils.isEmpty(Config.IGNORE_TABLE_PREFIX)){
            tableNames.removeIf(tableName ->Config.IGNORE_TABLE_PREFIX.stream().anyMatch(tableName::startsWith));
        }
        return tableNames;
    }
}
