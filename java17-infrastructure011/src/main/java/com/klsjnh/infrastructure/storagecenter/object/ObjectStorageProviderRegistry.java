package com.klsjnh.infrastructure.storagecenter.object;

/*                ObjectStorageProviderRegistry class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  provider-driven storage factory (open registry)
 *
 */

import com.klsjnh.domain.storagecenter.object.ObjectStorageFactoryPort;
import com.klsjnh.domain.storagecenter.object.ObjectStoragePort;
import com.klsjnh.domain.storagecenter.object.ObjectStorageProviderFactory;
import com.klsjnh.domain.storagecenter.object.StorageConnectionConfig;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds an {@link ObjectStoragePort} adapter by looking up the provider code in
 * an open registry of {@link ObjectStorageProviderFactory} beans. Consumers add
 * a provider by declaring one more bean; the platform is not modified.
 */

@Component
public class ObjectStorageProviderRegistry implements ObjectStorageFactoryPort {

    /**
     * Provider code to factory index, built from every injected factory bean.
     */
    private final Map<String, ObjectStorageProviderFactory> byProvider = new HashMap<>();

    /**
     * Create the registry from all provider factories on the classpath.
     *
     * @param factories provider factories
     */
    public ObjectStorageProviderRegistry(List<ObjectStorageProviderFactory> factories) {
        for (ObjectStorageProviderFactory factory : factories) {
            for (String code : factory.providers()) {
                byProvider.put(code, factory);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public ObjectStoragePort create(StorageConnectionConfig config) {
        String provider = config == null ? null : config.provider();
        ObjectStorageProviderFactory factory = provider == null ? null : byProvider.get(provider);

        if (factory == null) {
            throw new IllegalArgumentException("unknown storage provider: " + provider);
        }

        return factory.create(config);
    }
}
