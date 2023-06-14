package com.kewen.framework.boot.auth;

import com.kewen.framework.boot.auth.security.annotation.SecurityIgnore;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.*;

/**
 * 允许放行的url配置
 *
 * @author kewen
 * @since 2023-06-14
 */
public class PermitUrlContainer implements InitializingBean {

    @Autowired
    private AuthProperties authProperties;

    private final List<String> annotationPermits=new ArrayList<>();

    @Autowired
    RequestMappingHandlerMapping requestMappingHandlerMapping;

    public String[] getPermitUrls() {

        Collection<String> list = new HashSet<>();

        list.addAll(annotationPermits);
        list.addAll(Arrays.asList(authProperties.getPermitUrls()));

        return list.toArray(new String[0]);
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping.getHandlerMethods();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            RequestMappingInfo requestMappingInfo = entry.getKey();
            Set<String> patterns = requestMappingInfo.getPatternsCondition().getPatterns();
            HandlerMethod value = entry.getValue();
            boolean hasAnnotation = value.hasMethodAnnotation(SecurityIgnore.class);
            if (hasAnnotation) {
                annotationPermits.addAll(patterns);
            } else {
                boolean annotationPresent = value.getMethod().getDeclaringClass().isAnnotationPresent(SecurityIgnore.class);
                if (annotationPresent) {
                    annotationPermits.addAll(patterns);
                }
            }
        }
    }
}