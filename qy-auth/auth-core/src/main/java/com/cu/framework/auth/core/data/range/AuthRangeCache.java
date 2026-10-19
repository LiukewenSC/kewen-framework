package com.cu.framework.auth.core.data.range;

import com.cu.framework.auth.core.AuthDataRange;
import org.apache.ibatis.binding.MapperRegistry;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据范围声明缓存。
 * <p>
 * 声明位置是 MyBatis 的 Mapper 接口方法，缓存键是映射语句 id（{@code 接口全限定名.方法名}）。
 * 由于 MyBatis 不允许同名重载的映射方法，语句 id 与"一条语句"一一对应，
 * 因此权限条件的作用域天然等于一条语句，不需要任何跨语句的上下文。
 * <p>
 * 缓存内容只有注解决定的静态信息，不含任何用户相关数据；权限集合在执行期才写入。
 *
 * @author kewen
 * @since 2026-10-19
 */
public class AuthRangeCache {

    /** 语句 id -> 声明模板，仅含解析到查询语句的声明；扫描完成前为空 */
    private volatile Map<String, AuthRange> templates = Collections.emptyMap();

    /** 全部声明点，含解析失败的，供启动校验定位问题 */
    private volatile List<Declaration> declarations = Collections.emptyList();

    /**
     * 遍历 MyBatis 已注册的 Mapper 接口，收集所有 {@link AuthDataRange} 声明。
     * 需要在所有单例（含 Mapper 接口注册）就绪之后调用，否则漏掉的正是解析不到语句的那一类错误
     *
     * @param configuration MyBatis 配置
     */
    public void scan(Configuration configuration) {
        MapperRegistry mapperRegistry = configuration.getMapperRegistry();
        List<Declaration> declarationList = new ArrayList<>();
        Map<String, AuthRange> templateMap = new HashMap<>();

        for (Class<?> mapperInterface : mapperRegistry.getMappers()) {
            for (Method method : mapperInterface.getMethods()) {
                AuthDataRange annotation = method.getAnnotation(AuthDataRange.class);
                if (annotation == null) {
                    continue;
                }
                String statementId = mapperInterface.getName() + "." + method.getName();
                MappedStatement mappedStatement = configuration.hasStatement(statementId, false)
                        ? configuration.getMappedStatement(statementId, false)
                        : null;
                declarationList.add(new Declaration(statementId, mapperInterface, method, annotation, mappedStatement));

                if (mappedStatement != null && mappedStatement.getSqlCommandType() == SqlCommandType.SELECT) {
                    templateMap.put(statementId, toTemplate(annotation));
                }
            }
        }

        this.declarations = Collections.unmodifiableList(declarationList);
        this.templates = Collections.unmodifiableMap(templateMap);
    }

    /**
     * 由注解构造不含权限集合的声明模板
     * @param annotation Mapper 方法上的声明
     * @return 声明模板
     */
    private static AuthRange toTemplate(AuthDataRange annotation) {
        return new AuthRange()
                .setBusinessFunction(annotation.businessFunction())
                .setOperate(annotation.operate())
                .setTable(annotation.table())
                .setTableAlias(annotation.tableAlias())
                .setDataIdColumn(annotation.dataIdColumn())
                .setMatchMethod(annotation.matchMethod());
    }

    /**
     * 查某条语句的声明模板
     * @param statementId 映射语句 id
     * @return 声明模板，该语句未声明数据范围时返回 null
     */
    public AuthRange find(String statementId) {
        return templates.get(statementId);
    }

    /**
     * @return 全部声明点，供启动校验遍历
     */
    public List<Declaration> declarations() {
        return declarations;
    }

    /**
     * 单个声明点及其解析结果
     */
    public static class Declaration {

        private final String statementId;
        private final Class<?> mapperInterface;
        private final Method method;
        private final AuthDataRange annotation;
        private final MappedStatement mappedStatement;

        Declaration(String statementId, Class<?> mapperInterface, Method method,
                    AuthDataRange annotation, MappedStatement mappedStatement) {
            this.statementId = statementId;
            this.mapperInterface = mapperInterface;
            this.method = method;
            this.annotation = annotation;
            this.mappedStatement = mappedStatement;
        }

        /**
         * @return 按 {@code 接口全限定名.方法名} 拼出的语句 id，解析不到语句时也保留
         */
        public String getStatementId() {
            return statementId;
        }

        /**
         * @return 声明所在的 Mapper 接口
         */
        public Class<?> getMapperInterface() {
            return mapperInterface;
        }

        /**
         * @return 声明所在的方法
         */
        public Method getMethod() {
            return method;
        }

        /**
         * @return 方法上的声明
         */
        public AuthDataRange getAnnotation() {
            return annotation;
        }

        /**
         * @return 解析到的映射语句，解析不到时为 null
         */
        public MappedStatement getMappedStatement() {
            return mappedStatement;
        }
    }
}
