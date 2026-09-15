package com.klsjnh.domain.datasource;

/*                DynamicDataSourceRegistryPort interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate 2026.09.15
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  dynamic data source registry port interface
 *      2026.09.15  add reloadAll for the table-driven source
 *
 */

import java.util.List;

/**
 * Registry port for dynamic datasources: configs come from the table-driven
 * source (july_datasource) on reload, with the yaml ci011 list as the bootstrap
 * baseline; pools connect lazily on first use. The primary MyBatis-Plus
 * datasource is never part of this registry.
 */

public interface DynamicDataSourceRegistryPort {

    /**
     * Declare a datasource config (no connection attempt); the pool connects
     * lazily on first routing call.
     *
     * @param info connection info
     */
    void register(ConnectionInfo info);

    /**
     * Replace the whole declared set in one shot and reconcile the built pools
     * against it (table-driven reload entry).
     * <p>
     * Reconciliation rules: a config whose physical target (dsUrl /
     * username / password / dsType) is unchanged keeps its built pool; a
     * config that disappeared, got disabled or changed target has its pool
     * closed; a new config is declared but its pool stays unbuilt until first
     * use (the reload never pre-connects).
     * </p>
     *
     * @param infos the complete new declared set, never null
     * @return reconciliation summary, never null
     */
    ReloadResult reloadAll(List<ConnectionInfo> infos);

    /**
     * Remove a datasource and close its pool if one was built.
     *
     * @param dsCode datasource code
     */
    void unregister(String dsCode);

    /**
     * Whether a datasource config is declared.
     *
     * @param dsCode datasource code
     * @return true when declared
     */
    boolean isRegistered(String dsCode);

    /**
     * Probe a connection without caching a pool.
     *
     * @param info connection info
     * @return true when a connection could be established
     */
    boolean testConnection(ConnectionInfo info);
}
