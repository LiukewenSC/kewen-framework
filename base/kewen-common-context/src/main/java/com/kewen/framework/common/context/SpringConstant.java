package com.kewen.framework.common.context;

public interface SpringConstant {
    /**
     * ConfigurationClassParser 常量，修改ConfigurationClassParser 默认 PropertySourceFactory用到
     * ${@link org.springframework.context.annotation.ConfigurationClassParser#DEFAULT_PROPERTY_SOURCE_FACTORY}
     */
    String CONFIGURATION_CLASS_PARSER_NAME = "org.springframework.context.annotation.ConfigurationClassParser";
}
