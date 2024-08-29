package com.kewen.framework.boot.basic.p6spy;


import com.p6spy.engine.common.ConnectionInformation;
import com.p6spy.engine.common.P6LogQuery;
import com.p6spy.engine.event.JdbcEventListener;
import com.p6spy.engine.spy.JdbcEventListenerFactory;
import com.p6spy.engine.spy.P6ModuleManager;
import com.p6spy.engine.wrapper.ConnectionWrapper;

import java.sql.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * 
 * @author kewen
 * @since 2024-08-16
 */
public class KP6SpyDriver implements Driver {
    private static Driver instance = new KP6SpyDriver();
    private static JdbcEventListenerFactory jdbcEventListenerFactory;

    static {
        try {
            DriverManager.registerDriver(KP6SpyDriver.instance);
        } catch (SQLException e) {
            throw new IllegalStateException("Could not register KP6SpyDriver with DriverManager", e);
        }
    }

    @Override
    public boolean acceptsURL(final String url) {
        //return url != null && url.startsWith("jdbc:p6spy:");
        return url != null && url.startsWith("jdbc:");
    }

    /**
     * Parses out the real JDBC connection URL by removing "p6spy:".
     *
     * @param url the connection URL
     * @return the parsed URL
     */
    private String extractRealUrl(String url) {
        //return acceptsURL(url) ? url.replace("p6spy:", "") : url;
        return url;
    }

    static List<Driver> registeredDrivers() {
        List<Driver> result = new ArrayList<Driver>();
        for (Enumeration<Driver> driverEnumeration = DriverManager.getDrivers(); driverEnumeration.hasMoreElements(); ) {
            result.add(driverEnumeration.nextElement());
        }
        return result;
    }

    @Override
    public Connection connect(String url, Properties properties) throws SQLException {
        // if there is no url, we have problems
        if (url == null) {
            throw new SQLException("url is required");
        }

        if( !acceptsURL(url) ) {
            return null;
        }

        // find the real driver for the URL
        Driver passThru = findPassthru(url);

        P6LogQuery.debug("this is " + this + " and passthru is " + passThru);

        final long start = System.nanoTime();

        if (KP6SpyDriver.jdbcEventListenerFactory == null) {
            KP6SpyDriver.jdbcEventListenerFactory = JdbcEventListenerFactoryLoader.load();
        }
        final Connection conn;
        final JdbcEventListener jdbcEventListener = KP6SpyDriver.jdbcEventListenerFactory.createJdbcEventListener();
        final ConnectionInformation connectionInformation = ConnectionInformation.fromDriver(passThru);
        connectionInformation.setUrl(url);
        jdbcEventListener.onBeforeGetConnection(connectionInformation);
        try {
            conn =  passThru.connect(extractRealUrl(url), properties);
            connectionInformation.setConnection(conn);
            connectionInformation.setTimeToGetConnectionNs(System.nanoTime() - start);
            jdbcEventListener.onAfterGetConnection(connectionInformation, null);
        } catch (SQLException e) {
            connectionInformation.setTimeToGetConnectionNs(System.nanoTime() - start);
            jdbcEventListener.onAfterGetConnection(connectionInformation, e);
            throw e;
        }

        return ConnectionWrapper.wrap(conn, jdbcEventListener, connectionInformation);
    }

    protected Driver findPassthru(String url) throws SQLException {
        // registers the passthru drivers, if configured s
        P6ModuleManager.getInstance();

        String realUrl = extractRealUrl(url);
        Driver passthru = null;
        for (Driver driver: registeredDrivers() ) {
            try {
                if (driver.acceptsURL(extractRealUrl(url))) {
                    passthru = driver;
                    break;
                }
            } catch (SQLException e) {
            }
        }
        if( passthru == null ) {
            throw new SQLException("Unable to find a driver that accepts " + realUrl);
        }
        return passthru;
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties properties) throws SQLException {
        return findPassthru(url).getPropertyInfo(url, properties);
    }

    @Override
    public int getMajorVersion() {
        // This is a bit of a problem since there is no URL to determine the passthru!
        return 2;
    }

    @Override
    public int getMinorVersion() {
        // This is a bit of a problem since there is no URL to determine the passthru!
        return 0;
    }

    @Override
    public boolean jdbcCompliant() {
        // This is a bit of a problem since there is no URL to determine the passthru!
        return true;
    }

    // Note: @Override annotation not added to allow compilation using Java 1.6
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("Feature not supported");
    }

    public static void setJdbcEventListenerFactory(JdbcEventListenerFactory jdbcEventListenerFactory) {
        KP6SpyDriver.jdbcEventListenerFactory = jdbcEventListenerFactory;
    }
}
