package com.klsjnh.infrastructure.storagecenter.storage.seed;

/*                StorageSeed011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  storage seed 011 class
 *
 */

import lombok.extern.slf4j.Slf4j;

import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.storagecenter.object.StorageDefaultsPort;
import com.klsjnh.domain.storagecenter.object.StorageProviderCodes011;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;
import com.klsjnh.domain.storagecenter.storage.JulyStorageProvider;
import com.klsjnh.domain.storagecenter.storage.JulyStorageProviderBucket;
import com.klsjnh.domain.storagecenter.storage.JulyStorageProviderBucketRepository;
import com.klsjnh.domain.storagecenter.storage.JulyStorageProviderRepository;

import com.klsjnh.infrastructure.storagecenter.object.StorageProperties;

import org.springframework.stereotype.Component;

/**
 * Bootstrap seed: turns {@code krt.storage-center.*} into a {@code default}
 * provider row plus its default bucket child, one time. The yaml is a seed,
 * never a second source of truth — after the first boot the tables are the sole
 * runtime authority.
 */

@Slf4j
@Component
public class StorageSeed011 {

    /**
     * Seeded instance code.
     */
    private static final String SEED_CODE = "default";

    /**
     * Storage provider repository.
     */
    private final JulyStorageProviderRepository repository;

    /**
     * Storage bucket repository.
     */
    private final JulyStorageProviderBucketRepository bucketRepository;

    /**
     * Bootstrap properties.
     */
    private final StorageProperties properties;

    /**
     * Config-backed storage defaults (default bucket).
     */
    private final StorageDefaultsPort defaults;

    /**
     * Create the seed.
     *
     * @param repository       storage provider repository
     * @param bucketRepository storage bucket repository
     * @param properties       storage properties
     * @param defaults         config-backed storage defaults
     */
    public StorageSeed011(JulyStorageProviderRepository repository,
            JulyStorageProviderBucketRepository bucketRepository, StorageProperties properties,
            StorageDefaultsPort defaults) {
        this.repository = repository;
        this.bucketRepository = bucketRepository;
        this.properties = properties;
        this.defaults = defaults;
    }

    /**
     * Seed the default instance and its default bucket when never seen.
     */
    public void seed() {
        try {
            if (repository.existsIncludingDeleted(SEED_CODE)) {
                log.info("storage seed skipped: {} already exists ...", SEED_CODE);
                JulyStorageProvider existing = repository.findByCode(SEED_CODE);
                if (existing != null) {
                    seedDefaultBucket(existing.id().value(), defaults.defaultBucket());
                }
                return;
            }

            String type = properties.getDefaultType();

            if (!StorageProviderCodes011.LOCAL.equals(type) && !StorageProviderCodes011.MINIO.equals(type)
                    && !StorageProviderCodes011.S3.equals(type)) {
                log.warn("storage seed skipped: non built-in default-type {} ...", properties.getDefaultType());
                return;
            }

            boolean local = StorageProviderCodes011.LOCAL.equals(type);
            String basePath = local ? properties.getLocal011().getBasePath() : null;
            String bucket = defaults.defaultBucket();

            JulyStorageProvider storage = JulyStorageProvider.create(EntityId.generate(), SEED_CODE, 1, "默认存储",
                    type, basePath, local ? null : properties.getMinio011().getEndpoint(),
                    local ? null : properties.getMinio011().getAccessKey(),
                    local ? null : properties.getMinio011().getSecretKey(), false, 3600,
                    "seeded from krt.storage-center", AuditInfo.empty());

            repository.insert(storage);
            log.info("storage seed inserted {} ({}) ...", SEED_CODE, type);

            seedDefaultBucket(storage.id().value(), bucket);
        } catch (Exception ex) {
            log.warn("storage seed failed: {} ...", ex.getMessage());
        }
    }

    /**
     * Seed the default bucket child when the instance has no bucket rows.
     *
     * @param masterId storage provider id
     * @param bucket   resolved default bucket
     */
    private void seedDefaultBucket(String masterId, String bucket) {
        if (StringUtil011.isBlank(bucket) || bucketRepository.hasAnyIncludingDeleted(masterId)) {
            return;
        }

        JulyStorageProviderBucket bucketRow = JulyStorageProviderBucket.create(EntityId.generate(), masterId, bucket,
                bucket, true, 1, "seeded from krt.storage-center", AuditInfo.empty());

        bucketRepository.insert(bucketRow);
        log.info("storage seed inserted default bucket {} ...", bucket);
    }
}
