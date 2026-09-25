package com.klsjnh.observability;

/*                StorageObservabilityAutoConfiguration011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.25
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.25  storage health auto configuration (class-level conditional
 *                  so a missing storage starter never loads its types)
 *
 */

import com.klsjnh.domain.storagecenter.storage.JulyStorageProviderRepository;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

/**
 * Registers the storage health indicator only when the storage starter is on
 * the classpath. Declared through the auto-configuration imports file because
 * a plain {@code @Component} would fail class loading when storage is absent.
 */

@AutoConfiguration
@ConditionalOnClass(JulyStorageProviderRepository.class)
public class StorageObservabilityAutoConfiguration011 {

    /**
     * Register the storage provider health indicator.
     *
     * @param repository storage provider repository provider
     * @return health indicator
     */
    @Bean
    public HealthIndicator storageProviderHealthIndicator(ObjectProvider<JulyStorageProviderRepository> repository) {
        return new StorageProviderHealthIndicator011(repository);
    }
}
