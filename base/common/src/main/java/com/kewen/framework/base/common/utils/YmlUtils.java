package com.kewen.framework.base.common.utils;

import com.kewen.framework.base.common.factory.YmlPropertySourceFactory;
import org.springframework.beans.factory.config.YamlProcessor;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.CollectionFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.ResourceUtils;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Resources;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
        String[] keys = name.split("\\.");
        InputStream in = null;
        try {

            in = getInputStream(filePath);

            Yaml props = new Yaml();
            Map map = props.loadAs(in, Map.class);

            return MapUtil.getValue(map,name,clazz);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally {
            if (in != null){
                try {
                    in.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
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
        Resource[] resources = new Resource[paths.length];
        for (int i = 0; i < paths.length; i++) {
            String path=paths[i];
            if (path.startsWith("classpath:")){
                path=path.substring("classpath:".length());
            }
            resources[i]=new ClassPathResource(path);
        }
        return parse2Properties(resources);
    }
    public static Properties parse2PropertiesBak(Resource... resources){
        YamlPropertiesFactoryBean bean = new YamlPropertiesFactoryBean();
        bean.setResources(resources);
        return bean.getObject();
    }
    public static Properties parse2Properties(Resource... resources){
        return YamlUtilProcessor.process(resources);
    }

    private static class YamlUtilProcessor extends YamlProcessor {

        public static Properties process(Resource... resources){
            return new YamlUtilProcessor(resources).createProperties();
        }
        private YamlUtilProcessor(Resource... resources) {
            this.setResources(resources);
        }
        protected Properties createProperties() {
            Properties result = CollectionFactory.createStringAdaptingProperties();
            process(new MatchCallback() {
                @Override
                public void process(Properties properties, Map<String, Object> map) {
                    result.putAll(properties);
                     map.get("key");
                }
            });
            return result;
        }
    }
}
