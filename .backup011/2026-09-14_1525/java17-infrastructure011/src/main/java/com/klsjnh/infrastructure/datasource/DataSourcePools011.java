package com.klsjnh.infrastructure.datasource;

/*                DataSourcePools011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  data source pools 011 class
 *
 */

import com.klsjnh.common.enums.DatabaseType011;
import com.klsjnh.common.exception.BusinessException;

import com.klsjnh.domain.datasource.ConnectionInfo;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.stereotype.Component;

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
     * Build a single-connection probe pool for connectivity tests (not cached).
     *
     * @param info connection info
     * @return probe pool
     */
    public DruidDataSource buildForProbe(ConnectionInfo info) {
        DruidDataSource pool = build(info);
        pool.setMaxActive(1);

        return pool;
    }

    /**
     * Build a Druid pool from connection info (no wall filter — the routing
     * port is read-write by design).
     *
     * @param info connection info
     * @return configured pool
     */
    private DruidDataSource build(ConnectionInfo info) {
        DatabaseType011 type = DatabaseType011.fromString(info.dsType());

        if (type == null) {
            throw BusinessException.badRequest("unknown database type: " + info.dsType());
        }

        DruidDataSource pool = new DruidDataSource();
        pool.setName(info.dsCode());
        pool.setUrl(info.dsUrl());
        pool.setUsername(info.username());
        pool.setPassword(info.password());
        pool.setDriverClassName(type.getDriverClassName());
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
}
