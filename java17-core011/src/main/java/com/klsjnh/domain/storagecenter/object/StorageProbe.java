package com.klsjnh.domain.storagecenter.object;

/*                StorageProbe record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  connection probe result (legacy-aligned payload)
 *
 */

/**
 * Connection probe result, mirroring the legacy {@code testConnection} payload:
 * a success flag, the bucket count, the location ({@code basePath} for the
 * local adapter, {@code endpoint} for the S3 / MinIO family) and a message.
 *
 * @param success     whether the storage answered
 * @param bucketCount number of buckets seen (0 on failure)
 * @param basePath    local base path, null for remote adapters
 * @param endpoint    remote endpoint, null for the local adapter
 * @param message     client-safe detail ("ok" on success)
 */

public record StorageProbe(boolean success, int bucketCount, String basePath, String endpoint, String message) {

    /**
     * Build a local-adapter probe (basePath only).
     *
     * @param success     whether the storage answered
     * @param bucketCount bucket count
     * @param basePath    local base path
     * @param message     detail
     * @return probe result
     */
    public static StorageProbe local(boolean success, int bucketCount, String basePath, String message) {
        return new StorageProbe(success, bucketCount, basePath, null, message);
    }

    /**
     * Build a remote-adapter probe (endpoint only).
     *
     * @param success     whether the storage answered
     * @param bucketCount bucket count
     * @param endpoint    remote endpoint
     * @param message     detail
     * @return probe result
     */
    public static StorageProbe remote(boolean success, int bucketCount, String endpoint, String message) {
        return new StorageProbe(success, bucketCount, null, endpoint, message);
    }
}
