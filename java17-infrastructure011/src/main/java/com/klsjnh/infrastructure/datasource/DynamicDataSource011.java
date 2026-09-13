package com.klsjnh.infrastructure.datasource;

/*                DynamicDataSource011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  dynamic data source 011 class
 *
 */

import com.alibaba.druid.pool.DruidDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Routing datasource for dynamic targets: without a routing key it delegates
 * to the primary datasource (the MyBatis-Plus world stays untouched); with a
 * key set by SqlRoutingExecutor it resolves the dynamic pool by dsCode.
 * <p>
 * Never registered as a Spring bean — it lives only inside SqlRoutingExecutor,
 * so the primary datasource stays the single DataSource bean of the context.
 * </p>
 */

@Slf4j
public class DynamicDataSource011 extends AbstractRoutingDataSource {

    /**
     * Per-thread routing key.
     */
    private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();

    /**
     * Built dynamic pools by datasource code (populated lazily by the
     * registry's ensurePool).
     */
    private static final Map<String, DruidDataSource> DATA_SOURCES = new ConcurrentHashMap<>();

    /**
     * Fallback target when no routing key is set (the primary datasource).
     */
    private final DataSource defaultTarget;

    /**
     * Create the routing datasource.
     *
     * @param defaultTarget primary datasource fallback
     */
    public DynamicDataSource011(DataSource defaultTarget) {
        this.defaultTarget = defaultTarget;
    }

    /**
     * Set the routing key of the current thread.
     *
     * @param dataSourceLookupKey datasource code
     */
    public static void set(String dataSourceLookupKey) {
        CONTEXT_HOLDER.set(dataSourceLookupKey);
    }

    /**
     * Clear the routing key of the current thread.
     */
    public static void clear() {
        CONTEXT_HOLDER.remove();
    }

    /**
     * Register (or replace) a built pool; a replaced old pool is closed.
     *
     * @param dataSourceName datasource code
     * @param dataSource     druid pool
     */
    public static void add(String dataSourceName, DruidDataSource dataSource) {
        DruidDataSource old = DATA_SOURCES.put(dataSourceName, dataSource);

        if (old != null && old != dataSource) {
            try {
                old.close();
            } catch (Exception ex) {
                log.warn("close old data source {} failed {} ...", dataSourceName, ex.getMessage());
            }
        }
    }

    /**
     * Remove and close a pool.
     *
     * @param dataSourceName datasource code
     */
    public static void remove(String dataSourceName) {
        DruidDataSource old = DATA_SOURCES.remove(dataSourceName);

        if (old != null) {
            try {
                old.close();
            } catch (Exception ex) {
                log.warn("close data source {} failed {} ...", dataSourceName, ex.getMessage());
            }
        }
    }

    /**
     * Resolve the current target: the dynamic pool when a key is set, the
     * primary datasource otherwise.
     *
     * @return target datasource
     */
    @Override
    protected DataSource determineTargetDataSource() {
        String key = CONTEXT_HOLDER.get();

        if (key == null) {
            return defaultTarget;
        }

        DataSource target = DATA_SOURCES.get(key);

        if (target == null) {
            throw new IllegalStateException("datasource pool not built: " + key);
        }

        return target;
    }

    /**
     * Current routing key (unused by this class's own resolution, kept for
     * framework compatibility).
     *
     * @return routing key or null
     */
    @Override
    protected Object determineCurrentLookupKey() {
        return CONTEXT_HOLDER.get();
    }
}
