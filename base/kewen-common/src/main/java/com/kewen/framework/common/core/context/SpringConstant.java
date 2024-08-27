package com.kewen.framework.common.core.context;

/**
 * spring的上下文常量
 * @author kewen
 * @since 2023-04-26
 */
public interface SpringConstant {
    /**
     * ConfigurationClassParser 常量，修改ConfigurationClassParser 默认 PropertySourceFactory用到
     * ${@link org.springframework.context.annotation.ConfigurationClassParser#DEFAULT_PROPERTY_SOURCE_FACTORY}
     */
    String CONFIGURATION_CLASS_PARSER_NAME = "org.springframework.context.annotation.ConfigurationClassParser";
}
