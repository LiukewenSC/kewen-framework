package com.cu.framework.boot.basic.context;

import com.cu.framework.basic.utils.YmlUtils;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.DefaultPropertySourceFactory;
import org.springframework.core.io.support.EncodedResource;
import java.io.IOException;
import java.util.Properties;

/**
 * @descrpition yml配置文件解析器
 *  启动时加载yml的文件
 * @author kewen
 * @since 2022-12-13 14:27
 */
public class YmlPropertySourceFactory  extends DefaultPropertySourceFactory {
    /**
     * 创建属性源：资源不存在时返回空属性源；.yml/.yaml 文件用 YmlUtils 解析为 Properties；其余交给父类默认处理
     * @param name 属性源名称，为 null 时取资源文件名
     * @param resource 待解析的编码资源
     * @return 解析后的属性源
     * @throws IOException 读取资源失败时抛出
     */
    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
        String sourceName = name != null ? name : resource.getResource().getFilename();
        if (!resource.getResource().exists()) {
            return new PropertiesPropertySource(sourceName, new Properties());
        } else if (sourceName.endsWith(".yml") || sourceName.endsWith(".yaml")) {
            Properties propertiesFromYaml = YmlUtils.parse2Properties(resource.getResource());
            return new PropertiesPropertySource(sourceName, propertiesFromYaml);
            // 返回 yaml 属性资源 springboot 方式
            //return new YamlPropertySourceLoader ()
            //        .load (resource.getResource ().getFilename (), resource.getResource ())
            //        .get (0);
        } else {
            return super.createPropertySource(name, resource);
        }
    }
}
