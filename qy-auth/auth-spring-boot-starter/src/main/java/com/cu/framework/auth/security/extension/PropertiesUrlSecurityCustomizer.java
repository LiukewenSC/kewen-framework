package com.cu.framework.auth.security.extension;

import com.cu.framework.auth.security.annotation.SecurityIgnore;
import com.cu.framework.auth.security.config.UrlSecurityCustomizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 允许放行的url配置
 *
 * @author kewen
 * @since 2024-07-05
 */
@Slf4j
public class PropertiesUrlSecurityCustomizer implements ApplicationContextAware, UrlSecurityCustomizer {

    /** 配置项 cu.auth.permit-url 指定的放行 URL，多个以分号或逗号分隔 */
    @Value("${cu.auth.permit-url:}")
    private String permitUrl;

    /** 容器启动时从标注 @SecurityIgnore 的 Controller 类/方法上收集到的 URL 模式 */
    private final List<String> annotationPermits=new ArrayList<>();

    /**
     * 汇总注解收集与配置项 cu.auth.permit-url 两部分的放行 URL，去重后返回
     * @return 放行 URL 数组
     */
    public String[] getPermitUrls() {

        Collection<String> list = new HashSet<>();

        list.addAll(annotationPermits);
        if (StringUtils.hasText(permitUrl)){
            String[] permitUrls = StringUtils.tokenizeToStringArray(permitUrl, ";,");
            list.addAll(Arrays.asList(permitUrls));
        }
        return list.toArray(new String[0]);
    }
    /**
     * 容器注入回调：扫描 RequestMappingHandlerMapping 中所有映射方法，
     * 将方法或其所在类标注了 @SecurityIgnore 的 URL 收集到放行列表
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        //从RequestMappingHandlerMapping中找到Controller中的方法，提取加了SecurityIgnore注解的以从security中放开不进行登录验证
        RequestMappingHandlerMapping requestMappingHandlerMapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping.getHandlerMethods();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            RequestMappingInfo requestMappingInfo = entry.getKey();
            Set<String> patterns = extractPatterns(requestMappingInfo);
            HandlerMethod value = entry.getValue();
            boolean hasAnnotation = value.hasMethodAnnotation(SecurityIgnore.class);
            if (hasAnnotation || value.getMethod().getDeclaringClass().isAnnotationPresent(SecurityIgnore.class)) {
                annotationPermits.addAll(patterns);
            }
        }
    }

    /**
     * 从RequestMappingInfo中提取URL模式，兼容Spring MVC 5.3+（Spring Boot 2.6+）
     * 5.3+ 默认使用PathPatternParser，getPatternsCondition()会返回null，需要使用getPathPatternsCondition()
     */
    private Set<String> extractPatterns(RequestMappingInfo requestMappingInfo) {
        if (requestMappingInfo.getPatternsCondition() != null) {
            return requestMappingInfo.getPatternsCondition().getPatterns();
        }
        if (requestMappingInfo.getPathPatternsCondition() != null) {
            return requestMappingInfo.getPathPatternsCondition().getPatterns()
                    .stream()
                    .map(Object::toString)
                    .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    /**
     * UrlSecurityCustomizer 实现，返回全部放行 URL 供安全过滤链 permitAll
     * @return 放行 URL 列表
     */
    @Override
    public List<String> permitAll()  {
        return Arrays.asList(getPermitUrls());
    }
}
