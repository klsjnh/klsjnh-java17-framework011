package com.klsjnh.infrastructure.storagecenter.object.provider;

/*                MinioStorageProviderFactory class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  built-in minio011 / s3011 provider factory
 *
 */

import com.klsjnh.domain.storagecenter.object.ObjectStoragePort;
import com.klsjnh.domain.storagecenter.object.ObjectStorageProviderFactory;
import com.klsjnh.domain.storagecenter.object.StorageConnectionConfig;
import com.klsjnh.domain.storagecenter.object.StorageProviderCodes011;

import com.klsjnh.infrastructure.storagecenter.object.adapter.MinioObjectStorageAdapter;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Built-in provider factory for the MinIO / S3-compatible adapters
 * ({@code minio011}, {@code s3011}).
 */

@Component
public class MinioStorageProviderFactory implements ObjectStorageProviderFactory {

    /** {@inheritDoc} */
    @Override
    public List<String> providers() {
        return List.of(StorageProviderCodes011.MINIO, StorageProviderCodes011.S3);
    }

    /** {@inheritDoc} */
    @Override
    public ObjectStoragePort create(StorageConnectionConfig config) {
        return new MinioObjectStorageAdapter(config);
    }
}
