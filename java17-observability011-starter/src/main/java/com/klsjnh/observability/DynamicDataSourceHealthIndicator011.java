package com.klsjnh.observability;

/*                DynamicDataSourceHealthIndicator011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.25
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.25  dynamic datasource health indicator
 *
 */

import com.klsjnh.domain.datasource.kernel.ConnectionInfo;

import com.klsjnh.infrastructure.config.KrtDatasourceConfig011;
import com.klsjnh.infrastructure.datasource.kernel.DynamicDataSourceRegistryImpl;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Health indicator for the dynamic datasource kernel. Reports the configured
 * {@code krt.ci011} connections and which of them have a live Druid pool.
 * Pools connect lazily, so "not built yet" is normal and never reported DOWN;
 * no connection attempt is made here (health checks must not hammer external
 * databases).
 */

@Component
public class DynamicDataSourceHealthIndicator011 implements HealthIndicator {

    /**
     * Bootstrap connection list.
     */
    private final KrtDatasourceConfig011 krtConfig;

    /**
     * Dynamic datasource registry, present when the data starter is on the
     * classpath.
     */
    private final ObjectProvider<DynamicDataSourceRegistryImpl> registry;

    /**
     * Create the indicator.
     *
     * @param krtConfig bootstrap connection list
     * @param registry  dynamic datasource registry provider
     */
    public DynamicDataSourceHealthIndicator011(KrtDatasourceConfig011 krtConfig,
            ObjectProvider<DynamicDataSourceRegistryImpl> registry) {
        this.krtConfig = krtConfig;
        this.registry = registry;
    }

    /** {@inheritDoc} */
    @Override
    public Health health() {
        DynamicDataSourceRegistryImpl dynamicRegistry = registry.getIfAvailable();
        Map<String, String> pools = new HashMap<>();
        int built = 0;

        for (ConnectionInfo info : krtConfig.getCi011()) {
            boolean registered = dynamicRegistry != null && dynamicRegistry.isRegistered(info.dsCode());

            if (registered) {
                built = built + 1;
            }

            pools.put(info.dsCode(), registered ? "pool built" : "declared (lazy)");
        }

        return Health.up()
                .withDetail("configured", krtConfig.getCi011().size())
                .withDetail("poolsBuilt", built)
                .withDetail("pools", pools)
                .build();
    }
}
