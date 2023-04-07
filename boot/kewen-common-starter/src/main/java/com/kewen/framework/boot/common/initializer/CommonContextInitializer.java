package com.kewen.framework.boot.common.initializer;

import com.kewen.framework.boot.common.SpringConstant;
import com.kewen.framework.base.common.utils.BeanUtil;
import com.kewen.framework.boot.common.YmlPropertySourceFactory;
import org.springframework.beans.factory.parsing.ProblemReporter;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

import java.lang.reflect.Constructor;

/**
 * @descrpition 通用的 上下文初始化处理器，用于修改共有的上下文变化
 * @author kewen
 * @since 2023-02-03
 */
public class CommonContextInitializer implements ApplicationContextInitializer {

    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {

        //修改ConfigurationClassParser的默认xml解析器
        modifyConfigurationClassParser();
    }
    /**
     * 修改ConfigurationClassParser的默认xml解析器，兼容yml的，
     * 这种修改改了字节码 ， 没办法，谁叫它class都没给 public权限，而且还是final的静态变量
     */
    private void modifyConfigurationClassParser(){
        try {
            Class<?> aClass = Class.forName(SpringConstant.CONFIGURATION_CLASS_PARSER_NAME);
            Constructor<?> constructor = aClass.getDeclaredConstructor(
                    MetadataReaderFactory.class, ProblemReporter.class, Environment.class,
                    ResourceLoader.class, BeanNameGenerator.class, BeanDefinitionRegistry.class
            );
            constructor.setAccessible(true);
            Object configurationClassParser = constructor.newInstance(null, null, null, null, null, null);
            BeanUtil.setFinalField(configurationClassParser,"DEFAULT_PROPERTY_SOURCE_FACTORY",new YmlPropertySourceFactory());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}