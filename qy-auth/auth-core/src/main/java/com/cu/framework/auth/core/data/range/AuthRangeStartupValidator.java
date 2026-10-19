package com.cu.framework.auth.core.data.range;

import com.cu.framework.auth.core.AuthDataRange;
import net.sf.jsqlparser.JSQLParserException;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.scripting.defaults.RawSqlSource;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据范围声明的启动期校验器。
 * <p>
 * 挂载在 {@link SmartInitializingSingleton} 上：所有单例（含 MyBatis mapper 接口的注册）都已就绪，
 * 又早于任何业务调用，因此配置错误让应用启动失败，而不是留到运行期静默放开过滤。
 * <p>
 * 校验分四项：声明解析不到映射语句、语句不是查询、语句启用了二级缓存、静态可得的 SQL 里匹配不到目标表；
 * 另外检查是否还有 {@link AuthDataRange} 残留在 Mapper 接口以外的位置。
 *
 * @author kewen
 * @since 2026-10-19
 */
public class AuthRangeStartupValidator implements SmartInitializingSingleton {

    private static final Logger log = LoggerFactory.getLogger(AuthRangeStartupValidator.class);

    private final SqlSessionFactory sqlSessionFactory;
    private final AuthRangeCache authRangeCache;
    private final SqlAuthInject sqlAuthInject;
    private final ListableBeanFactory beanFactory;

    public AuthRangeStartupValidator(SqlSessionFactory sqlSessionFactory,
                                     AuthRangeCache authRangeCache,
                                     SqlAuthInject sqlAuthInject,
                                     ListableBeanFactory beanFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
        this.authRangeCache = authRangeCache;
        this.sqlAuthInject = sqlAuthInject;
        this.beanFactory = beanFactory;
        log.info("@AuthDataRange 数据范围声明启动校验器已启动");
    }

    @Override
    public void afterSingletonsInstantiated() {

        Configuration configuration = sqlSessionFactory.getConfiguration();
        authRangeCache.scan(configuration);

        List<String> errors = new ArrayList<>();
        for (AuthRangeCache.Declaration declaration : authRangeCache.declarations()) {
            validate(declaration, errors);
        }
        validateLegacyPlacements(configuration, errors);

        if (!errors.isEmpty()) {
            throw new IllegalStateException("@AuthDataRange 数据范围声明校验失败：" + String.join("；", errors));
        }
        log.debug("数据范围声明校验通过，受管辖的查询语句共 {} 条", authRangeCache.declarations().size());
    }

    /**
     * 校验单个声明点：能否解析到语句、语句类型、是否与二级缓存冲突、静态 SQL 中的目标表是否存在
     *
     * @param declaration 声明点
     * @param errors      错误收集容器
     */
    private void validate(AuthRangeCache.Declaration declaration, List<String> errors) {
        String position = declaration.getMapperInterface().getName() + "#" + declaration.getMethod().getName();
        MappedStatement ms = declaration.getMappedStatement();
        if (ms == null) {
            errors.add("声明<" + position + ">解析不到映射语句<" + declaration.getStatementId()
                    + ">，请检查方法名与映射语句 id 是否一致");
            return;
        }

        if (ms.getSqlCommandType() != SqlCommandType.SELECT) {
            errors.add("声明<" + declaration.getStatementId() + ">对应的是 " + ms.getSqlCommandType()
                    + " 语句，数据范围只作用于查询语句");
            return;
        }

        if (ms.getCache() != null) {
            errors.add("声明<" + declaration.getStatementId() + ">所在的映射语句启用了二级缓存，"
                    + "缓存键在权限条件注入之前就已计算，两者不能共存，请去掉该语句的二级缓存或去掉声明");
            return;
        }

        validateTargetTable(declaration, ms, errors);
    }

    /**
     * 只对静态可得的 SQL 校验目标表。
     * 动态 SQL 用 null 参数渲染会真的执行 OGNL 判断，得出一条与实际执行不同的 SQL，据此判定表名会产生误报；
     * 语句提供器生成的 SQL 则会触发 provider 调用。这两类统一延后到执行期，由注入时的匹配失败抛异常兜住
     *
     * @param declaration 声明点
     * @param ms          映射语句
     * @param errors      错误收集容器
     */
    private void validateTargetTable(AuthRangeCache.Declaration declaration, MappedStatement ms, List<String> errors) {
        if (!(ms.getSqlSource() instanceof RawSqlSource)) {
            if (log.isDebugEnabled()) {
                log.debug("语句<{}>的 SQL 只能运行期拼出，跳过目标表启动校验", declaration.getStatementId());
            }
            return;
        }

        AuthRange template = authRangeCache.find(declaration.getStatementId());
        try {
            sqlAuthInject.validateTargetTable(ms.getBoundSql(null).getSql(), template);
        } catch (JSQLParserException e) {
            errors.add("声明<" + declaration.getStatementId() + ">的目标表在该语句中不存在：" + e.getMessage());
        }
    }

    /**
     * 迁移守卫：数据范围声明只能出现在 Mapper 接口方法上。
     * 旧用法把注解放 in Controller/Service，切面删除后那种位置不会被上面的 mapper 扫描看到，
     * 会变成静默不过滤，因此这里显式让它启动失败
     *
     * @param configuration MyBatis 配置，用于判定某个类是否是 mapper 接口
     * @param errors        错误收集容器
     */
    private void validateLegacyPlacements(Configuration configuration, List<String> errors) {
        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            Class<?> beanType;
            try {
                beanType = beanFactory.getType(beanName);
            } catch (Exception e) {
                continue;
            }
            if (beanType == null) {
                continue;
            }
            Class<?> userClass = ClassUtils.getUserClass(beanType);
            if (configuration.getMapperRegistry().hasMapper(userClass)) {
                continue;
            }
            for (Method method : ReflectionUtils.getAllDeclaredMethods(userClass)) {
                if (method.isAnnotationPresent(AuthDataRange.class)) {
                    errors.add("声明<" + userClass.getName() + "#" + method.getName()
                            + ">不在 MyBatis Mapper 接口上，数据范围声明必须标在 Mapper 的查询方法上");
                }
            }
        }
    }
}
