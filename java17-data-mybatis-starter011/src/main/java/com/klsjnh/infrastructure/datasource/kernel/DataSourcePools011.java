package com.klsjnh.infrastructure.datasource.kernel;

/*                DataSourcePools011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate 2026.09.15
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  data source pools 011 class
 *      2026.09.15  add schema / driver override to probe pool
 *
 */

import com.klsjnh.common.constant.DatabaseTypes011;
import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.datasource.kernel.ConnectionInfo;

import org.springframework.stereotype.Component;

import com.alibaba.druid.pool.DruidDataSource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Druid pool builder and lazy pool cache for dynamic datasources: the pool is
 * built (not connected) on first request per dsCode — Druid opens physical
 * connections on first getConnection, which makes the whole chain lazy.
 */

@Component
public class DataSourcePools011 {

    /**
     * Probe pool connect timeout in milliseconds (10s, product decision).
     */
    private static final int PROBE_MAX_WAIT_MILLIS = 10_000;

    /**
     * Lazy pool cache by datasource code.
     */
    private final Map<String, DruidDataSource> pools = new ConcurrentHashMap<>();

    /**
     * Get or lazily build the pool of a datasource; driver/type errors throw
     * immediately, connection errors surface at first getConnection.
     *
     * @param info connection info
     * @return the cached druid pool
     */
    public DruidDataSource get(ConnectionInfo info) {
        return pools.computeIfAbsent(info.dsCode(), k -> build(info));
    }

    /**
     * Close and forget the pool of a datasource (unregister path).
     *
     * @param dsCode datasource code
     */
    public void close(String dsCode) {
        DruidDataSource old = pools.remove(dsCode);

        if (old != null) {
            old.close();
        }
    }

    /**
     * Build a single-connection probe pool for connectivity tests (never
     * cached, never fed back into the routing registry).
     * <p>
     * Probe profile (product decision): {@code initialSize 0} (nothing opened
     * at build time), {@code maxActive 1} (a single physical connection),
     * {@code maxWait 10s} (bounded connect timeout). The caller MUST close the
     * pool in a finally block — Druid pools hold real sockets.
     * </p>
     *
     * @param info connection info
     * @return probe pool, closed by the caller
     */
    public DruidDataSource buildForProbe(ConnectionInfo info) {
        DruidDataSource pool = build(info);
        pool.setName("probe-" + info.dsCode());
        pool.setMaxActive(1);
        pool.setMinIdle(0);
        pool.setMaxWait(PROBE_MAX_WAIT_MILLIS);
        pool.setTestOnBorrow(true);
        pool.setTestWhileIdle(false);

        return pool;
    }

    /**
     * Whether a pool is currently cached for a datasource code.
     *
     * @param dsCode datasource code
     * @return true when a pool is cached
     */
    public boolean isBuilt(String dsCode) {
        return pools.containsKey(dsCode);
    }

    /**
     * Build a Druid pool from connection info (no wall filter — the routing
     * port is read-write by design).
     *
     * @param info connection info
     * @return configured pool
     */
    private DruidDataSource build(ConnectionInfo info) {
        DruidDataSource pool = new DruidDataSource();
        pool.setName(info.dsCode());
        pool.setUrl(info.dsUrl());
        pool.setUsername(info.username());
        pool.setPassword(info.password());
        pool.setDriverClassName(resolveDriverClass(info));
        pool.setInitialSize(0);
        pool.setMinIdle(0);
        pool.setMaxActive(100);
        pool.setMaxWait(60000);
        pool.setTimeBetweenEvictionRunsMillis(60000);
        pool.setMinEvictableIdleTimeMillis(300000);
        pool.setTestWhileIdle(true);
        pool.setTestOnBorrow(false);
        pool.setTestOnReturn(false);
        pool.setPoolPreparedStatements(true);
        pool.setMaxPoolPreparedStatementPerConnectionSize(20);

        return pool;
    }

    /**
     * Resolve the JDBC driver class: an explicit driverClass wins, otherwise
     * the database type default is used; an unknown type without an explicit
     * driver is a configuration error.
     *
     * @param info connection info
     * @return driver class name
     */
    private String resolveDriverClass(ConnectionInfo info) {
        if (!StringUtil011.isBlank(info.driverClass())) {
            return info.driverClass();
        }

        String driverClass = DatabaseTypes011.driverClass(info.dsType());

        if (driverClass == null) {
            throw BusinessException.badRequest("unknown database type: " + info.dsType());
        }

        return driverClass;
    }
}
