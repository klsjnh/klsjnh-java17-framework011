package com.klsjnh.observability;

/*                StorageProviderHealthIndicator011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.25
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.25  storage provider health indicator
 *
 */

import com.klsjnh.domain.storagecenter.storage.JulyStorageProviderRepository;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Health indicator for the storage center management table. Verifies the
 * persistence layer answers and reports whether the seeded {@code default}
 * provider row exists. Adapter connectivity is deliberately NOT probed —
 * resolving a provider builds a client (and may create buckets), which is a
 * side effect a health check must not trigger.
 */

@Component
public class StorageProviderHealthIndicator011 implements HealthIndicator {

    /**
     * Marker code of the seeded default provider row.
     */
    private static final String DEFAULT_PROVIDER_CODE = "default";

    /**
     * Storage provider repository, present when core is on the classpath.
     */
    private final ObjectProvider<JulyStorageProviderRepository> repository;

    /**
     * Create the indicator.
     *
     * @param repository storage provider repository provider
     */
    public StorageProviderHealthIndicator011(ObjectProvider<JulyStorageProviderRepository> repository) {
        this.repository = repository;
    }

    /** {@inheritDoc} */
    @Override
    public Health health() {
        JulyStorageProviderRepository repo = repository.getIfAvailable();

        if (repo == null) {
            return Health.up().withDetail("defaultProvider", "storage center not configured").build();
        }

        try {
            boolean hasDefault = repo.findEnabledByCode(DEFAULT_PROVIDER_CODE) != null;

            return Health.up()
                    .withDetail("defaultProvider", hasDefault ? "present" : "missing (run seeding)")
                    .build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
