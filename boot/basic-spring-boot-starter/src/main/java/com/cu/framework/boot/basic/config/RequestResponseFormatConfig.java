package com.cu.framework.boot.basic.config;

import com.cu.framework.basic.exception.GlobalExceptionHandlerAdvice;
import com.cu.framework.basic.logger.trace.TraceResponseBodyAdvice;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.core.convert.converter.Converter;
import tools.jackson.databind.ext.javatime.deser.LocalDateDeserializer;
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;
import tools.jackson.databind.module.SimpleModule;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;


/**
 *  请求返回参数解析配置，
 * @author kewen
 * @since 2023-04-27
 */
@AutoConfiguration
public class RequestResponseFormatConfig {


    /**
     * 异常统一返回增强
     * @return
     */
    @Bean
    GlobalExceptionHandlerAdvice exceptionHandlerAdvance(){
        return new GlobalExceptionHandlerAdvice();
    }
    /**
     * 链路追踪响应体增强，把 TraceContext 中的流水号信息写入返回的 Result
     * @return 链路追踪 ResponseBodyAdvice
     */
    @Bean
    TraceResponseBodyAdvice trackingLogResponseBodyAdvice(){
        return new TraceResponseBodyAdvice();
    }

    /**
     * 配置 body序列化 jackson全局转换规则
     *
     * @return
     */
    @Bean
    public JsonMapperBuilderCustomizer jacksonJsonMapperBuilderCustomizer() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(LocalDate.class,
                new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        module.addDeserializer(LocalDate.class,
                new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        module.addSerializer(LocalDateTime.class,
                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        module.addDeserializer(LocalDateTime.class,
                new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return builder -> builder
                .defaultDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
                .addModule(module);
    }

    /**
     * 字符串转 Date 转换器，按 yyyy-MM-dd HH:mm:ss 格式解析
     * @return String 到 Date 的转换器
     */
    @Bean
    public Converter<String, Date> dateConverter() {
        return new Converter<String, Date>() {
            @Override
            public Date convert(String source) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    return formatter.parse(source);
                } catch (Exception e) {
                    throw new RuntimeException(String.format("Error parsing %s to Date", source));
                }
            }
        };
    }

    /**
     * 字符串转 LocalDate 转换器，按 yyyy-MM-dd 格式解析
     * @return String 到 LocalDate 的转换器
     */
    @Bean
    public Converter<String, LocalDate> localDateConverter() {
        return new Converter<String, LocalDate>() {
            @Override
            public LocalDate convert(String source) {
                return LocalDate.parse(source, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
        };
    }

    /**
     * 字符串转 LocalDateTime 转换器，按 yyyy-MM-dd HH:mm:ss 格式解析
     * @return String 到 LocalDateTime 的转换器
     */
    @Bean
    public Converter<String, LocalDateTime> localDateTimeConverter() {
        return new Converter<String, LocalDateTime>() {
            @Override
            public LocalDateTime convert(String source) {
                return LocalDateTime.parse(source, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
        };
    }

}
