package com.klsjnh.domain.storage;

/*                StorageConnectionConfig class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  storage connection config class
 *
 */

/**
 * Immutable connection settings an {@link ObjectStoragePort} adapter is built
 * from: a {@code july_storage} row (runtime) or the yaml bootstrap entry.
 *
 * @param provider              storage type code (StorageType011)
 * @param basePath              local root (local011)
 * @param endpoint              endpoint (S3 family)
 * @param accessKey             access key (S3 family)
 * @param secretKey             secret key (S3 family)
 * @param secure                whether to use HTTPS (S3 family)
 * @param defaultBucket         default bucket
 * @param presignExpirySeconds  presigned URL expiry in seconds
 */

public record StorageConnectionConfig(String provider, String basePath, String endpoint, String accessKey,
        String secretKey, boolean secure, String defaultBucket, int presignExpirySeconds) {
}
