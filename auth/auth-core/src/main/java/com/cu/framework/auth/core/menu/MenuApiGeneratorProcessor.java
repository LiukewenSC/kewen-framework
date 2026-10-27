package com.cu.framework.auth.core.menu;

import com.cu.framework.auth.core.AuthMenu;
import com.cu.framework.auth.core.AuthMenuDoc;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * API菜单生成处理器
 *  有MenuApiStore才执行
 * @author kewen
 * @since 2024-08-09
 */
public class MenuApiGeneratorProcessor implements ApplicationContextAware,Runnable {

    private static final Logger log = LoggerFactory.getLogger(MenuApiGeneratorProcessor.class);
    /** 菜单API入库服务，容器中不存在时 run 不执行生成 */
    private MenuApiService menuApiService;
    /** 容器中全部 Controller 请求方法与映射信息的对应关系 */
    private Map<RequestMappingInfo, HandlerMethod> handlerMethods;

    /** 本次扫描收集到的 路径->菜单实体 映射，key 为请求路径 */
    private final Map<String, MenuApiEntity> menuApiMap = new ConcurrentHashMap<>();

    /**
     * 从容器中获取 RequestMappingHandlerMapping 的全部请求方法映射，
     * 并获取可选的 MenuApiService（容器中不存在时保持为 null）
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        RequestMappingHandlerMapping handlerMapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
        this.handlerMethods = handlerMapping.getHandlerMethods();
        MenuApiService menuApiService = applicationContext.getBeanProvider(MenuApiService.class).getIfAvailable();
        if (menuApiService != null){
            this.menuApiService = menuApiService;
        }
    }

    /**
     * 菜单API生成主流程：扫描 @AuthMenu 相关的 Controller 构建 路径->菜单 映射，
     * 过滤掉数据库中已存在的路径，为新菜单生成ID并按 parentPath 组装 parentId，
     * 最后批量入库；MenuApiService 不存在时直接跳过
     */
    @Override
    public void run() {
        //处理对应关系，找到路径信息，在此处处理不会引发系统启动的问题
        if (menuApiService ==null){
            log.warn("menuApiService is null");
            return;
        }
        process();

        //获取数据库中的API
        List<MenuApiEntity> dbs = menuApiService.list();
        Set<String> dbPaths = dbs.stream().map(MenuApiEntity::getPath).collect(Collectors.toSet());

        //过滤掉数据库中已经包含的API，只创建不包含的
        List<MenuApiEntity> readySaveApis = menuApiMap.entrySet().stream()
                .filter(entry -> !dbPaths.contains(entry.getKey()))  //在数据库中有的则不计入本次列表
                //根据path排序
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(entry -> {
                    //排序之后用雪花算法生成ID
                    MenuApiEntity apiEntity = entry.getValue();
                    apiEntity.setId(menuApiService.generateId());
                    return apiEntity;
                }).collect(Collectors.toList());

        //把数据库的还要加到map中去，后续要使用父path找parentId，而且这里应该允许父级覆盖当前
        for (MenuApiEntity db : dbs) {
            menuApiMap.put(db.getPath(), db);
        }

        //组装上级ID
        List<MenuApiEntity> apiSaves = readySaveApis.stream()
                .peek(apiEntity -> {
                    //如果父path和自己是相同的，则把自己设置为根
                    if (apiEntity.getPath().equals(apiEntity.getParentPath()) || apiEntity.getParentPath() == null) {
                        apiEntity.setParentId(menuApiService.getRootParentId());
                        return;
                    }
                    //按 parentPath 取父：类上 @RequestMapping 值为空或 "/" 时未生成虚拟路径，
                    //或父路径本身尚未入库且未落在本次扫描集合内，都会让这里取到 null；
                    //此时降级为根节点，避免因单条父链断裂让整个菜单生成线程抛 NPE 静默失败
                    MenuApiEntity parent = menuApiMap.get(apiEntity.getParentPath());
                    if (parent == null || parent.getId() == null) {
                        log.warn("菜单 <{}> 的父路径 <{}> 未在已注册菜单中找到，按根节点入库",
                                apiEntity.getPath(), apiEntity.getParentPath());
                        apiEntity.setParentId(menuApiService.getRootParentId());
                        return;
                    }
                    apiEntity.setParentId(parent.getId());
                }).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(apiSaves)) {
            menuApiService.saveBatch(apiSaves);
            log.info("添加的接口有: {}",apiSaves.stream().map(MenuApiEntity::getPath).collect(Collectors.joining(";")));
        } else {
            log.info("没有生成新的API接口");
        }
    }

    /**
     * 处理AuthMenu注解相关的path和Controller的关系
     */
    private void process() {
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            /*
                path始终以RequestmappingHandlerMapping为准
                管辖由 @AuthMenu 声明，标在类上即覆盖本类全部端点；
                标签由 @AuthMenuDoc 声明，controller 标签与 method 标签合并生成
                类还需要单独生成一个请求，以创建上下层级关系
             */
            String controllerName;
            String controllerPath;

            RequestMappingInfo mappingInfo = entry.getKey();
            HandlerMethod handlerMethod = entry.getValue();
            AuthMenuDoc authMenuDoc;
            boolean hasAuthMenu = false;
            boolean hasAddClassVirtualPath = false;


            //处理类上的注解
            Class<?> controllerClass = handlerMethod.getMethod().getDeclaringClass();
            if (controllerClass.getAnnotation(AuthMenu.class) != null) {
                //类上有@AuthMenu注解，本类全部端点受管辖
                hasAuthMenu = true;
            }
            //类上没有@AuthMenuDoc时以类简单名为准，仍然按类建立上下级关系
            if ((authMenuDoc = controllerClass.getAnnotation(AuthMenuDoc.class)) != null) {
                controllerName = authMenuDoc.name();
            } else {
                controllerName =controllerClass.getSimpleName();
            }

            //获取controller的 路径，如果都没有标注RequestMapping或者没有路径，那就算了，不做上下级的虚拟路径了
            RequestMapping annRequestMapping = controllerClass.getAnnotation(RequestMapping.class);
            if (annRequestMapping != null && annRequestMapping.value().length !=0) {
                //创建class的路径，这个路径基本上是虚拟的，只加入一次，但是可以被基于方法的覆盖
                controllerPath = annRequestMapping.value()[0];
                //这里如果Controller只有@RequestMapping，但是没有加路径或者为/根路径的话，也不添加做虚拟路径
                if (StringUtils.isNotBlank(controllerPath) && !controllerPath.equals("/")) {
                    hasAddClassVirtualPath = true;
                }
            } else {
                controllerPath = null;
            }

            //处理方法上的注解，方法注解可能方法上有AuthMenu注解，也可能只有Controller上有注解
            if (handlerMethod.hasMethodAnnotation(AuthMenu.class)) {
                hasAuthMenu = true;
            }
            //方法上没有@AuthMenuDoc时以方法名为准
            authMenuDoc = handlerMethod.getMethodAnnotation(AuthMenuDoc.class);
            String methodName = controllerName + ">" + (authMenuDoc != null
                    ? authMenuDoc.name()
                    : handlerMethod.getMethod().getName());

            //判断有菜单API注解才执行
            if (hasAuthMenu) {
                //有权限注解，而且需要添加controller虚拟路径才添加
                if (hasAddClassVirtualPath){
                    menuApiMap.putIfAbsent(controllerPath, MenuApiEntity.of(controllerPath, controllerName));
                }
                putMenuAuth(mappingInfo, methodName, controllerPath);
            }
        }
    }


    /**
     * @param mappingInfo controller生成的信息
     * @param methodName  名称，不会为空
     */
    private void putMenuAuth(RequestMappingInfo mappingInfo, String methodName, String controllerPath) {
        Set<String> patterns = extractPatterns(mappingInfo);
        for (String pattern : patterns) {
            menuApiMap.putIfAbsent(pattern.trim(), MenuApiEntity.of(pattern, methodName, controllerPath));
        }
    }

    /**
     * 从RequestMappingInfo中提取URL模式，兼容Spring MVC 5.3+（Spring Boot 2.6+）
     * 5.3+ 默认使用PathPatternParser，getPatternsCondition()会返回null，需要使用getPathPatternsCondition()
     */
    private Set<String> extractPatterns(RequestMappingInfo mappingInfo) {
        if (mappingInfo.getPatternsCondition() != null) {
            return mappingInfo.getPatternsCondition().getPatterns();
        }
        if (mappingInfo.getPathPatternsCondition() != null) {
            return mappingInfo.getPathPatternsCondition().getPatterns()
                    .stream()
                    .map(Object::toString)
                    .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }
}
