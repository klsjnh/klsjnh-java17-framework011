package com.klsjnh.domain.storage;

/*                ObjectStoragePort interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  object storage port interface
 *      2026.09.13  add bucket ops and stat, align get/delete semantics
 *
 */

import java.util.List;

/**
 * Object storage port: one small surface served by per-vendor adapters
 * (local011 / minio011 first; cos011 / tos011 / oss011 / s3011 phased).
 * The active adapter is selected by {@code krt.storage-center.default-type}.
 * <p>
 * Semantic contract: {@link #get} returns null ONLY for a missing object —
 * any other failure (network, credentials) throws; {@link #delete} and
 * {@link #deleteBucket} are idempotent — true when the target is absent after
 * the call, loud failure when a bucket is not empty.
 * </p>
 */

public interface ObjectStoragePort {

    /**
     * The storage center's default bucket, read from configuration
     * ({@code krt.storage-center.default-bucket}).
     * <p>
     * Also the target of a blank bucket argument: callers that have no
     * bucket preference pass null/blank and land here.
     * </p>
     *
     * @return default bucket name, never blank
     */
    String defaultBucket();

    /**
     * Store an object.
     *
     * @param bucket      bucket (folder for the local adapter), blank falls
     *                    back to {@link #defaultBucket()}
     * @param key         object key, unique inside the bucket
     * @param content     object bytes
     * @param contentType mime type, nullable
     * @return the final stored key
     */
    String put(String bucket, String key, byte[] content, String contentType);

    /**
     * Read an object; null ONLY when the object is missing — any other
     * failure throws (a broken storage must never masquerade as an absent
     * object).
     *
     * @param bucket bucket
     * @param key    object key
     * @return object bytes, null when missing
     */
    byte[] get(String bucket, String key);

    /**
     * Delete an object (idempotent).
     *
     * @param bucket bucket
     * @param key    object key
     * @return true when the object is absent after the call (deleted or
     *         already absent)
     */
    boolean delete(String bucket, String key);

    /**
     * Whether an object exists.
     *
     * @param bucket bucket
     * @param key    object key
     * @return true when present
     */
    boolean exists(String bucket, String key);

    /**
     * Object metadata (size / last-modified / content-type) without
     * downloading the object.
     *
     * @param bucket bucket
     * @param key    object key
     * @return stat, null when the object is missing
     */
    ObjectStat stat(String bucket, String key);

    /**
     * List object keys under a prefix.
     *
     * @param bucket bucket
     * @param prefix key prefix, nullable for all
     * @return object keys
     */
    List<String> list(String bucket, String prefix);

    /**
     * Whether a bucket exists.
     *
     * @param bucket bucket name
     * @return true when present
     */
    boolean bucketExists(String bucket);

    /**
     * Create a bucket (folder for the local adapter); an existing bucket is a
     * no-op.
     *
     * @param bucket bucket name
     */
    void createBucket(String bucket);

    /**
     * Delete an EMPTY bucket (idempotent); a non-empty bucket is a loud
     * failure so data can never be silently orphaned.
     *
     * @param bucket bucket name
     * @return true when the bucket is absent after the call (deleted or
     *         already absent), false / loud failure otherwise
     */
    boolean deleteBucket(String bucket);

    /**
     * List all bucket names.
     *
     * @return bucket names
     */
    List<String> listBuckets();

    /**
     * Presigned GET URL for an object: a real time-limited URL for the S3 /
     * MinIO family, a {@code file:} URI for the local adapter (nothing to sign).
     *
     * @param bucket bucket
     * @param key    object key
     * @return presigned URL / URI
     */
    String presignedGetUrl(String bucket, String key);
}
