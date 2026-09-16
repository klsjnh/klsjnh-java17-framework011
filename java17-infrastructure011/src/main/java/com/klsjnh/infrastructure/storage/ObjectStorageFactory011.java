package com.klsjnh.infrastructure.storage;

/*                ObjectStorageFactory011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  object storage factory 011 class
 *
 */

import com.klsjnh.common.enums.StorageType011;

import com.klsjnh.domain.storage.ObjectStorageFactoryPort;
import com.klsjnh.domain.storage.ObjectStoragePort;
import com.klsjnh.domain.storage.StorageConnectionConfig;

import org.springframework.stereotype.Component;

/**
 * Adapter factory driven by the instance provider: local011 uses the local
 * disk adapter; minio011 and s3011 use the S3-compatible MinIO adapter. Other
 * vendors (cos011 / tos011 / oss011) land when their native SDKs are added.
 */

@Component
public class ObjectStorageFactory011 implements ObjectStorageFactoryPort {

    /**
     * Create an adapter for the given connection config.
     *
     * @param config connection config
     * @return storage adapter, never null
     */
    @Override
    public ObjectStoragePort create(StorageConnectionConfig config) {
        StorageType011 type = StorageType011.of(config.provider());

        if (type == null) {
            throw new IllegalArgumentException("unknown storage provider: " + config.provider());
        }

        switch (type) {
            case LOCAL011:
                return new LocalObjectStorageAdapter(config);
            case MINIO011:
            case S3011:
                return new MinioObjectStorageAdapter(config);
            default:
                throw new IllegalArgumentException("storage provider not implemented yet: " + config.provider());
        }
    }
}
