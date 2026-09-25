package com.klsjnh.infrastructure.storagecenter.object;

/*                StorageResolver011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  storage resolver 011 class
 *
 */

import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.storagecenter.storage.JulyStorageProvider;
import com.klsjnh.domain.storagecenter.storage.JulyStorageProviderBucket;
import com.klsjnh.domain.storagecenter.storage.JulyStorageProviderBucketRepository;
import com.klsjnh.domain.storagecenter.storage.JulyStorageProviderRepository;
import com.klsjnh.domain.storagecenter.object.ObjectStorageFactoryPort;
import com.klsjnh.domain.storagecenter.object.ObjectStoragePort;
import com.klsjnh.domain.storagecenter.object.StorageResolverPort;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Table-driven storage resolver: storage code → cached {@link ObjectStoragePort}
 * adapter built by the factory with the instance's default bucket. A blank code
 * falls back to the {@code default} instance (the yaml seed).
 */

@Component
public class StorageResolver011 implements StorageResolverPort {

    /**
     * Default instance code.
     */
    private static final String DEFAULT_CODE = "default";

    /**
     * Storage provider repository.
     */
    private final JulyStorageProviderRepository repository;

    /**
     * Storage bucket repository (default bucket lookup).
     */
    private final JulyStorageProviderBucketRepository bucketRepository;

    /**
     * Adapter factory.
     */
    private final ObjectStorageFactoryPort factory;

    /**
     * Cached adapters by storage code.
     */
    private final Map<String, ObjectStoragePort> cache = new ConcurrentHashMap<>();

    /**
     * Create the resolver.
     *
     * @param repository       storage provider repository
     * @param bucketRepository storage bucket repository
     * @param factory          adapter factory
     */
    public StorageResolver011(JulyStorageProviderRepository repository,
            JulyStorageProviderBucketRepository bucketRepository, ObjectStorageFactoryPort factory) {
        this.repository = repository;
        this.bucketRepository = bucketRepository;
        this.factory = factory;
    }

    /**
     * Resolve the adapter for a storage code.
     *
     * @param storageCode storage code, may be blank for the default instance
     * @return storage adapter, never null
     */
    @Override
    public ObjectStoragePort resolve(String storageCode) {
        String key = StringUtil011.isBlank(storageCode) ? DEFAULT_CODE : storageCode.trim();

        ObjectStoragePort cached = cache.get(key);

        if (cached != null) {
            return cached;
        }

        JulyStorageProvider storage = repository.findEnabledByCode(key);

        if (storage == null) {
            throw new IllegalStateException("storage is not available: " + key);
        }

        JulyStorageProviderBucket defaultBucket = bucketRepository.findDefault(storage.id().value());
        String defaultBucketName = defaultBucket == null ? null : defaultBucket.bucketName();
        ObjectStoragePort adapter = factory.create(storage.toConnectionConfig(defaultBucketName));
        cache.put(key, adapter);

        return adapter;
    }

    /**
     * Evict the cached adapter of a storage code.
     *
     * @param storageCode storage code
     */
    @Override
    public void evict(String storageCode) {
        if (!StringUtil011.isBlank(storageCode)) {
            cache.remove(storageCode.trim());
        }
    }
}
