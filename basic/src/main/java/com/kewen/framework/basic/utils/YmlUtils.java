package com.kewen.framework.basic.utils;

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
    public static Properties parse2PropertiesBak(Resource... resources){
        YamlPropertiesFactoryBean bean = new YamlPropertiesFactoryBean();
        bean.setResources(resources);
        return bean.getObject();
    }
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
