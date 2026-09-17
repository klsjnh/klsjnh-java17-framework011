package com.klsjnh.infrastructure.storagecenter.seed;

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

import com.klsjnh.common.enums.StorageType011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;
import com.klsjnh.domain.storagecenter.JulyStorage;
import com.klsjnh.domain.storagecenter.JulyStorageRepository;

import com.klsjnh.infrastructure.storagecenter.StorageProperties;

import org.springframework.stereotype.Component;

/**
 * Bootstrap seed: turns {@code krt.storage-center.*} into a {@code default}
 * row, one time. The yaml is a seed, never a second source of truth — after the
 * first boot the table is the sole runtime authority.
 */

@Slf4j
@Component
public class StorageSeed011 {

    /**
     * Seeded instance code.
     */
    private static final String SEED_CODE = "default";

    /**
     * Storage repository.
     */
    private final JulyStorageRepository repository;

    /**
     * Bootstrap properties.
     */
    private final StorageProperties properties;

    /**
     * Create the seed.
     *
     * @param repository storage repository
     * @param properties storage properties
     */
    public StorageSeed011(JulyStorageRepository repository, StorageProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    /**
     * Seed the default instance when it has never been seen.
     */
    public void seed() {
        try {
            if (repository.existsIncludingDeleted(SEED_CODE)) {
                log.info("storage seed skipped: {} already exists ...", SEED_CODE);
                return;
            }

            StorageType011 type = StorageType011.of(properties.getDefaultType());

            if (type == null) {
                log.warn("storage seed skipped: unknown default-type {} ...", properties.getDefaultType());
                return;
            }

            boolean local = type == StorageType011.LOCAL011;
            String basePath = local ? properties.getLocal011().getBasePath() : null;
            String bucket = StringUtil011.isBlank(properties.getMinio011().getDefaultBucket())
                    ? properties.getDefaultBucket()
                    : properties.getMinio011().getDefaultBucket();

            JulyStorage storage = JulyStorage.create(EntityId.generate(), SEED_CODE, 1, "默认存储",
                    type.getCode(), basePath, local ? null : properties.getMinio011().getEndpoint(),
                    local ? null : properties.getMinio011().getAccessKey(),
                    local ? null : properties.getMinio011().getSecretKey(), false, bucket, 3600,
                    "seeded from krt.storage-center", AuditInfo.empty());

            repository.insert(storage);
            log.info("storage seed inserted {} ({}) ...", SEED_CODE, type.getCode());
        } catch (Exception ex) {
            log.warn("storage seed failed: {} ...", ex.getMessage());
        }
    }
}
