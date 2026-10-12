package com.cu.framework.boot.basic.p6spy;


import com.p6spy.engine.spy.DefaultJdbcEventListenerFactory;
import com.p6spy.engine.spy.JdbcEventListenerFactory;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * 
 * @author kewen
 * @since 2024-08-16
 */
class JdbcEventListenerFactoryLoader {

    /** 单例工厂：优先取 ServiceLoader（SPI）加载到的第一个实现，没有则退回 DefaultJdbcEventListenerFactory */
    private static final JdbcEventListenerFactory jdbcEventListenerFactory;

    static {
        final Iterator<JdbcEventListenerFactory> iterator = ServiceLoader
                .load(JdbcEventListenerFactory.class, JdbcEventListenerFactory.class.getClassLoader())
                .iterator();
        if (iterator.hasNext()) {
            jdbcEventListenerFactory = iterator.next();
        } else {
            jdbcEventListenerFactory = new DefaultJdbcEventListenerFactory();
        }
    }

    /**
     * 私有构造器，防止实例化
     */
    private JdbcEventListenerFactoryLoader() {
    }

    /**
     * 获取已加载的 JdbcEventListenerFactory 单例
     * @return JDBC 事件监听器工厂
     */
    static JdbcEventListenerFactory load() {
        return jdbcEventListenerFactory;
    }

}
