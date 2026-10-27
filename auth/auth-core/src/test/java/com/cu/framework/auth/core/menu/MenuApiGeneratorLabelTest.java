package com.cu.framework.auth.core.menu;

import com.cu.framework.auth.core.AuthMenu;
import com.cu.framework.auth.core.AuthMenuDoc;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 菜单标签生成测试：管辖由 @AuthMenu 决定，标签由 @AuthMenuDoc 决定，缺失时回落类简单名与方法名。
 *
 * @author kewen
 * @since 2026-10-20
 */
public class MenuApiGeneratorLabelTest {

    /** 类上有管辖声明与文案，方法只靠类级的批量管辖 */
    @AuthMenu
    @AuthMenuDoc(name = "类文案")
    @RequestMapping("/class-doc")
    public static class ClassDocOnlyController {
        @GetMapping("/hello")
        public void hello() {
        }
    }

    /** 类与方法都有文案 */
    @AuthMenu
    @AuthMenuDoc(name = "类文案")
    @RequestMapping("/both-doc")
    public static class BothDocController {
        @AuthMenu
        @AuthMenuDoc(name = "方法文案")
        @GetMapping("/hello")
        public void hello() {
        }
    }

    /** 只有管辖声明，两处都没有文案 */
    @AuthMenu
    @RequestMapping("/no-doc")
    public static class NoDocController {
        @GetMapping("/hello")
        public void hello() {
        }
    }

    @Test
    public void classDocNamesTheGroupRowAndFallsThroughToMethodName() {
        Map<String, MenuApiEntity> rows = generate(ClassDocOnlyController.class, "hello", "/class-doc/hello");
        Assert.assertEquals("类文案", rows.get("/class-doc").getName());
        Assert.assertEquals("类文案>hello", rows.get("/class-doc/hello").getName());
    }

    @Test
    public void methodDocOverridesTheLabel() {
        Map<String, MenuApiEntity> rows = generate(BothDocController.class, "hello", "/both-doc/hello");
        Assert.assertEquals("类文案", rows.get("/both-doc").getName());
        Assert.assertEquals("类文案>方法文案", rows.get("/both-doc/hello").getName());
    }

    @Test
    public void missingDocFallsBackToClassSimpleNameAndMethodName() {
        Map<String, MenuApiEntity> rows = generate(NoDocController.class, "hello", "/no-doc/hello");
        Assert.assertEquals("NoDocController", rows.get("/no-doc").getName());
        Assert.assertEquals("NoDocController>hello", rows.get("/no-doc/hello").getName());
    }

    /**
     * 直接灌入 handler 映射后调用生成逻辑，取回 路径->菜单 映射
     *
     * @param controllerClass 被测 Controller
     * @param methodName      被测方法名
     * @param pattern         该方法映射出的请求路径
     * @return 路径 -> 菜单行
     */
    @SuppressWarnings("unchecked")
    private Map<String, MenuApiEntity> generate(Class<?> controllerClass, String methodName, String pattern) {
        try {
            MenuApiGeneratorProcessor processor = new MenuApiGeneratorProcessor();
            HandlerMethod handlerMethod = new HandlerMethod(controllerClass.getDeclaredConstructor().newInstance(), methodName);
            Map<RequestMappingInfo, HandlerMethod> handlerMethods = new LinkedHashMap<>();
            handlerMethods.put(RequestMappingInfo.paths(pattern).build(), handlerMethod);

            Field handlerMethodsField = ReflectionUtils.findField(MenuApiGeneratorProcessor.class, "handlerMethods");
            ReflectionUtils.makeAccessible(handlerMethodsField);
            ReflectionUtils.setField(handlerMethodsField, processor, handlerMethods);

            Method process = ReflectionUtils.findMethod(MenuApiGeneratorProcessor.class, "process");
            ReflectionUtils.makeAccessible(process);
            ReflectionUtils.invokeMethod(process, processor);

            Field menuApiMap = ReflectionUtils.findField(MenuApiGeneratorProcessor.class, "menuApiMap");
            ReflectionUtils.makeAccessible(menuApiMap);
            return (Map<String, MenuApiEntity>) ReflectionUtils.getField(menuApiMap, processor);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("构造被测 handler 失败", e);
        }
    }
}
