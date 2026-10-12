package com.cu.framework.basic.utils;

import org.springframework.beans.factory.config.YamlProcessor;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.CollectionFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.ResourceUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @descrpition 
 * @author kewen
 * @since 2022-11-30 15:58
 */
public class YmlUtils {

    /**
     * 解析 .yml文件得到对应的属性值
     * @param filePath
     * @param name
     * @param clazz  String.class 或者 Map.class
     * @param <T>
     * @return
     */
    public static <T> T parse(String filePath,String name,Class<T> clazz){
        Map<String, Object> map = YamlUtilProcessor.process(filePath).getMap();
        return MapUtil.getValue(map, name, clazz);
    }


    /**
     * 基于 Spring ResourceUtils 解析路径并打开文件输入流（支持 classpath: 等资源前缀）
     * @param filePath 文件路径
     * @return 带缓冲的文件输入流
     * @throws FileNotFoundException 文件不存在时抛出
     */
    private static InputStream getInputStream(String filePath) throws FileNotFoundException {
        /*
        //jdk方式
        if (filePath.startsWith("classpath:")){
            filePath=filePath.substring("classpath:".length());
        }
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath);
        */

        //spring方式
        File file = ResourceUtils.getFile(filePath);
        return new BufferedInputStream(new FileInputStream(file));
    }

    /**
     * 将yml文件转换为Properties
     * @param paths classpath路径
     * @return
     */
    public static Properties parse2Properties(String... paths){
        return YamlUtilProcessor.process(paths).getProperties() ;
    }
    /**
     * 使用 Spring 的 YamlPropertiesFactoryBean 将 yaml 资源转换为 Properties
     * @param resources yaml 资源
     * @return 解析后的 Properties
     */
    public static Properties parse2PropertiesBak(Resource... resources){
        YamlPropertiesFactoryBean bean = new YamlPropertiesFactoryBean();
        bean.setResources(resources);
        return bean.getObject();
    }
    /**
     * 通过 YamlUtilProcessor 将 yaml 资源解析为 Properties
     * @param resources yaml 资源
     * @return 解析后的 Properties
     */
    public static Properties parse2Properties(Resource... resources){
        return YamlUtilProcessor.process(resources).getProperties() ;
    }

    /**
     * Yaml接收对象
     */
    public static class YamlUtilProcessor extends YamlProcessor {

        /**
         * 解析后的Properties数据
         */
        private final Properties properties ;
        /**
         * 解析后的Map数据，
         */
        private final Map<String,Object> map;

        /**
         * 解析文件路径的到对象
         * @param paths  文件路径
         * @return
         */
        public static YamlUtilProcessor process(String... paths){
            Resource[] resources = new Resource[paths.length];
            for (int i = 0; i < paths.length; i++) {
                String path=paths[i];
                if (path.startsWith("classpath:")){
                    path=path.substring("classpath:".length());
                }
                resources[i]=new ClassPathResource(path);
            }
            return process(resources);
        }

        /**
         * 解析资源的到对象
         * @param resources 资源
         * @return
         */
        public static YamlUtilProcessor process(Resource... resources){
            return new YamlUtilProcessor(resources).init();
        }
        /**
         * 构造处理器，设置待解析资源并初始化承接解析结果的 Properties 与 Map 容器
         * @param resources yaml 资源
         */
        private YamlUtilProcessor(Resource... resources) {
            this.setResources(resources);
            this.properties=CollectionFactory.createStringAdaptingProperties();
            this.map =new HashMap<>();
        }

        /**
         * 根据 resources 解析成 properties 和 map 并填充
         * @return 加载完成的对象
         */
        protected YamlUtilProcessor init() {
            process((resultProperties, resultMap) -> {
                properties.putAll(resultProperties);
                map.putAll(resultMap);
            });
            return this;
        }

        public Properties getProperties() {
            return properties;
        }

        public Map<String, Object> getMap() {
            return map;
        }
    }
}
