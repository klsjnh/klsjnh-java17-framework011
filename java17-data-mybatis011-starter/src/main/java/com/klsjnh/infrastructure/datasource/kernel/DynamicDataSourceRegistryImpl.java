package com.klsjnh.infrastructure.datasource.kernel;

/*                DynamicDataSourceRegistryImpl class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate 2026.09.15
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  dynamic data source registry impl class
 *      2026.09.15  table-driven reloadAll with pool retention
 *
 */

import lombok.extern.slf4j.Slf4j;

import com.klsjnh.common.exception.BusinessException;

import com.klsjnh.domain.datasource.kernel.ConnectionInfo;
import com.klsjnh.domain.datasource.kernel.DynamicDataSourceRegistryPort;
import com.klsjnh.domain.datasource.kernel.ReloadResult;

import com.klsjnh.infrastructure.config.KrtDatasourceConfig011;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry implementation: declares datasource configs (yaml ci011 bootstrap
 * baseline, replaced by the table-driven snapshot on reload) with pools that
 * connect lazily via ensurePool on the first routing call for a dsCode.
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
    public DynamicDataSourceRegistryImpl(DataSourcePools011 pools, KrtDatasourceConfig011 krtConfig) {
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
     * Replace the whole declared set in one shot and reconcile the built pools
     * against it (table-driven reload entry).
     * <p>
     * The incoming snapshot is built into a fresh map first, so a failure
     * while enumerating it never leaves the registry half-updated. Pools of
     * configs whose physical target is unchanged are kept; pools of vanished,
     * disabled or retargeted configs are closed. New configs are declared but
     * never pre-connected — the reload itself opens zero connections.
     * </p>
     *
     * @param infos the complete new declared set, never null
     * @return reconciliation summary, never null
     */
    @Override
    public ReloadResult reloadAll(List<ConnectionInfo> infos) {
        String funcName = "reload all";

        if (infos == null) {
            throw BusinessException.badRequest(funcName + ": infos is required");
        }

        Map<String, ConnectionInfo> next = new HashMap<>();

        for (ConnectionInfo info : infos) {
            if (info == null || info.dsCode() == null || info.dsCode().isBlank()) {
                log.warn("{} skip invalid config ...", funcName);
                continue;
            }

            next.put(info.dsCode(), info);
        }

        int reused = 0;
        int closed = 0;
        int failed = 0;

        // Pass 1 — configs in the incoming snapshot: keep unchanged pools,
        // close the pool of a retargeted config before its new config lands.
        for (Map.Entry<String, ConnectionInfo> entry : next.entrySet()) {
            String dsCode = entry.getKey();
            ConnectionInfo before = configs.get(dsCode);
            ConnectionInfo after = entry.getValue();

            if (before != null && after.sameTarget(before)) {
                reused++;
                continue;
            }

            if (!pools.isBuilt(dsCode)) {
                continue;
            }

            try {
                DynamicDataSource011.remove(dsCode);
                pools.close(dsCode);
                closed++;
            } catch (Exception ex) {
                failed++;
                log.warn("{} close retargeted pool {} failed {} ...", funcName, dsCode, ex.getMessage());
            }
        }

        // Pass 2 — declared configs missing from the snapshot (deleted or
        // disabled): their pools must disappear from the routing table.
        for (String dsCode : configs.keySet()) {
            if (next.containsKey(dsCode)) {
                continue;
            }

            try {
                DynamicDataSource011.remove(dsCode);
                pools.close(dsCode);
                closed++;
            } catch (Exception ex) {
                failed++;
                log.warn("{} close removed pool {} failed {} ...", funcName, dsCode, ex.getMessage());
            }
        }

        configs.clear();
        configs.putAll(next);

        ReloadResult result = new ReloadResult(next.size(), configs.size(), reused, closed, failed);

        log.info("{} enabled {} registered {} reused {} closed {} failed {} ...",
                funcName, result.enabled(), result.registered(), reused, closed, failed);

        return result;
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
        pools.close(dsCode);
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
