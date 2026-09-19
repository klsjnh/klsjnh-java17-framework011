package com.klsjnh.infrastructure.storagecenter.object;

/*                StorageDefaults011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  config-backed storage defaults 011 class
 *
 */

import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.storagecenter.object.StorageProviderCodes011;
import com.klsjnh.domain.storagecenter.object.StorageDefaultsPort;

import org.springframework.stereotype.Component;

/**
 * Configuration-backed storage defaults ({@code krt.storage-center.*}): the
 * default bucket comes from the active adapter's {@code default-bucket}, so the
 * application layer can read it without depending on the infrastructure
 * properties type.
 */

@Component
public class StorageDefaults011 implements StorageDefaultsPort {

    /**
     * Top-level default bucket fallback.
     */
    private final StorageProperties properties;

    /**
     * Create the adapter.
     *
     * @param properties storage properties
     */
    public StorageDefaults011(StorageProperties properties) {
        this.properties = properties;
    }

    /**
     * The active adapter's default bucket, falling back to the top-level
     * {@code krt.storage-center.default-bucket}.
     *
     * @return default bucket name, blank when unset
     */
    @Override
    public String defaultBucket() {
        String type = properties.getDefaultType();
        String bucket;

        if (StorageProviderCodes011.LOCAL.equals(type)) {
            bucket = properties.getLocal011().getDefaultBucket();
        } else if (StorageProviderCodes011.MINIO.equals(type) || StorageProviderCodes011.S3.equals(type)) {
            bucket = properties.getMinio011().getDefaultBucket();
        } else {
            bucket = null;
        }

        return StringUtil011.isBlank(bucket) ? properties.getDefaultBucket() : bucket;
    }
}
