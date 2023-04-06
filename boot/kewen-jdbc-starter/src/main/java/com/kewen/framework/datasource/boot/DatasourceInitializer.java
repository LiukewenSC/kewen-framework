package com.kewen.framework.datasource.boot;

import com.kewen.framework.base.common.factory.YmlPropertySourceFactory;
import com.kewen.framework.base.common.model.SpringConstant;
import com.kewen.framework.base.common.utils.BeanUtil;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.parsing.ProblemReporter;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

import java.lang.reflect.Constructor;

public class DatasourceInitializer implements ApplicationContextInitializer {

    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {

        //修改ConfigurationClassParser的默认xml解析器
        modifyConfigurationClassParser();
    }
    /**
     * 修改ConfigurationClassParser的默认xml解析器
     */
    private void modifyConfigurationClassParser(){
        try {
            Class<?> aClass = Class.forName(SpringConstant.configurationClassParserName);
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