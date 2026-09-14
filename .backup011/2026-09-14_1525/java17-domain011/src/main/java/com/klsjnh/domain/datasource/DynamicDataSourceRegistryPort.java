package com.klsjnh.domain.datasource;

/*                DynamicDataSourceRegistryPort interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  dynamic data source registry port interface
 *
 */

/**
 * Registry port for dynamic datasources: configs are declared up front (yaml
 * ci011 / future table-driven), pools connect lazily on first use. The
 * primary MyBatis-Plus datasource is never part of this registry.
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
