package com.klsjnh.infrastructure.datasource;

/*                DynamicDataSourceRegistryImpl class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  dynamic data source registry impl class
 *
 */

import com.klsjnh.common.exception.BusinessException;

import com.klsjnh.domain.datasource.ConnectionInfo;
import com.klsjnh.domain.datasource.DynamicDataSourceRegistryPort;
import com.klsjnh.infrastructure.config.KrtConfig011;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry implementation: declares datasource configs at startup (yaml
 * ci011 — declaration only, zero connections) and builds pools lazily via
 * ensurePool on the first routing call for a dsCode.
 */

@Slf4j
@Component
public class DynamicDataSourceRegistryImpl implements DynamicDataSourceRegistryPort {

    /**
     * Declared configs by datasource code.
     */
    private final Map<String, ConnectionInfo> configs = new ConcurrentHashMap<>();

    /**
     * Pool builder with lazy cache.
     */
    private final DataSourcePools011 pools;

    /**
     * Create the registry and declare the yaml ci011 configs (no connections).
     *
     * @param pools     pool builder
     * @param krtConfig framework config (ci011 list)
     */
    public DynamicDataSourceRegistryImpl(DataSourcePools011 pools, KrtConfig011 krtConfig) {
        this.pools = pools;

        for (ConnectionInfo info : krtConfig.getCi011()) {
            configs.put(info.dsCode(), info);
        }

        log.info("dynamic datasource configs declared {} ...", configs.size());
    }

    /**
     * Declare a datasource config (no connection attempt).
     *
     * @param info connection info
     */
    @Override
    public void register(ConnectionInfo info) {
        String funcName = "register";

        configs.put(info.dsCode(), info);
        log.info("{} {} declared ...", funcName, info.dsCode());
    }

    /**
     * Remove a datasource and close its pool if one was built.
     *
     * @param dsCode datasource code
     */
    @Override
    public void unregister(String dsCode) {
        String funcName = "unregister";

        configs.remove(dsCode);
        DynamicDataSource011.remove(dsCode);
        log.info("{} {} done ...", funcName, dsCode);
    }

    /**
     * Whether a datasource config is declared.
     *
     * @param dsCode datasource code
     * @return true when declared
     */
    @Override
    public boolean isRegistered(String dsCode) {
        return configs.containsKey(dsCode);
    }

    /**
     * Probe a connection without caching a pool.
     *
     * @param info connection info
     * @return true when a connection could be established
     */
    @Override
    public boolean testConnection(ConnectionInfo info) {
        try (var ignored = pools.buildForProbe(info).getConnection()) {
            return true;
        } catch (Exception ex) {
            log.warn("test connection {} failed {} ...", info.dsCode(), ex.getMessage());

            return false;
        }
    }

    /**
     * Ensure the pool of a datasource is built and visible to the routing
     * datasource (lazy connect on first use).
     *
     * @param dsCode datasource code
     */
    public void ensurePool(String dsCode) {
        ConnectionInfo info = configs.get(dsCode);

        if (info == null) {
            throw BusinessException.badRequest("unknown datasource code: " + dsCode);
        }

        DynamicDataSource011.add(dsCode, pools.get(info));
    }

    /**
     * Get the declared config of a datasource (for dialect resolution).
     *
     * @param dsCode datasource code
     * @return connection info
     */
    public ConnectionInfo getConfig(String dsCode) {
        ConnectionInfo info = configs.get(dsCode);

        if (info == null) {
            throw BusinessException.badRequest("unknown datasource code: " + dsCode);
        }

        return info;
    }
}
