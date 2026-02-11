package com.kewen.framework.boot.basic.p6spy;


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

    private JdbcEventListenerFactoryLoader() {
    }

    static JdbcEventListenerFactory load() {
        return jdbcEventListenerFactory;
    }

}
