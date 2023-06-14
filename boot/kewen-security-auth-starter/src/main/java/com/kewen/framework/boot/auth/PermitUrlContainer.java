package com.kewen.framework.boot.auth;

import com.kewen.framework.boot.auth.security.annotation.SecurityIgnore;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.StringUtils;
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
public class PermitUrlContainer implements ApplicationContextAware {

    @Value("${kewen.auth.permit-url:}")
    String permitUrl;

    private final List<String> annotationPermits=new ArrayList<>();

    public String[] getPermitUrls() {

        Collection<String> list = new HashSet<>();

        list.addAll(annotationPermits);
        if (StringUtils.hasText(permitUrl)){
            String[] permitUrls = StringUtils.tokenizeToStringArray(permitUrl, ";,");
            list.addAll(Arrays.asList(permitUrls));
        }

        return list.toArray(new String[0]);
    }
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        RequestMappingHandlerMapping requestMappingHandlerMapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping.getHandlerMethods();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            RequestMappingInfo requestMappingInfo = entry.getKey();
            Set<String> patterns = requestMappingInfo.getPatternsCondition().getPatterns();
            HandlerMethod value = entry.getValue();
            boolean hasAnnotation = value.hasMethodAnnotation(SecurityIgnore.class);
            if (hasAnnotation || value.getMethod().getDeclaringClass().isAnnotationPresent(SecurityIgnore.class)) {
                annotationPermits.addAll(patterns);
            }
        }
    }
}
