package com.klsjnh.infrastructure.storagecenter;

/*                MinioObjectStorageAdapter class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  minio object storage adapter class
 *
 */

import lombok.extern.slf4j.Slf4j;

import com.klsjnh.domain.storagecenter.BucketInfo;
import com.klsjnh.domain.storagecenter.ObjectStat;
import com.klsjnh.domain.storagecenter.ObjectStoragePort;
import com.klsjnh.domain.storagecenter.StorageConnectionConfig;
import com.klsjnh.domain.storagecenter.StorageProbe;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.http.Method;
import io.minio.ListBucketsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveBucketArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Minio011 adapter: serves the self-hosted MinIO deployment via the MinIO
 * SDK. Active when default-type = minio011; the default bucket is
 * auto-created when missing.
 */

@Slf4j
public class MinioObjectStorageAdapter implements ObjectStoragePort {

    /**
     * MinIO client.
     */
    private final MinioClient client;

    /**
     * Connection config.
     */
    private final StorageConnectionConfig config;

    /**
     * Create the adapter and ensure the default bucket exists.
     *
     * @param config connection config
     */
    public MinioObjectStorageAdapter(StorageConnectionConfig config) {
        this.config = config;

        this.client = MinioClient.builder()
                .endpoint(config.endpoint())
                .credentials(config.accessKey(), config.secretKey())
                .build();

        try {
            String bucket = config.defaultBucket();

            if (bucket != null && !bucket.isBlank()
                    && !client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("storage bucket {} created ...", bucket);
            }
        } catch (Exception ex) {
            log.warn("storage bucket ensure failed {} ...", ex.getMessage());
        }
    }

    /**
     * The configured default bucket.
     *
     * @return default bucket name
     */
    @Override
    public String defaultBucket() {
        return config.defaultBucket();
    }

    /**
     * Presigned GET URL with the configured expiry.
     *
     * @param bucket bucket
     * @param key    object key
     * @return presigned URL
     */
    @Override
    public String presignedGetUrl(String bucket, String key) {
        try {
            return client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketOf(bucket))
                    .object(key)
                    .expiry(config.presignExpirySeconds())
                    .build());
        } catch (Exception ex) {
            throw new IllegalStateException("presign failed: " + ex.getMessage(), ex);
        }
    }

    /**
     * Store an object.
     *
     * @param bucket      bucket, blank falls back to the default bucket
     * @param key         object key
     * @param content     object bytes
     * @param contentType mime type, nullable
     * @return the final stored key
     */
    @Override
    public String put(String bucket, String key, byte[] content, String contentType) {
        try {
            client.putObject(PutObjectArgs.builder()
                    .bucket(bucketOf(bucket))
                    .object(key)
                    .stream(new ByteArrayInputStream(content), content.length, -1)
                    .contentType(contentType == null ? "application/octet-stream" : contentType)
                    .build());

            return key;
        } catch (Exception ex) {
            throw new IllegalStateException("put failed: " + ex.getMessage(), ex);
        }
    }

    /**
     * Read an object; null ONLY for a missing object (NoSuchKey) — any other
     * failure throws so a broken storage never masquerades as absent data.
     *
     * @param bucket bucket
     * @param key    object key
     * @return object bytes, null when missing
     */
    @Override
    public byte[] get(String bucket, String key) {
        try (var stream = client.getObject(GetObjectArgs.builder()
                .bucket(bucketOf(bucket))
                .object(key)
                .build())) {

            return stream.readAllBytes();
        } catch (ErrorResponseException ex) {
            if ("NoSuchKey".equals(ex.errorResponse().code())) {
                return null;
            }

            throw new IllegalStateException("get failed: " + ex.errorResponse().code(), ex);
        } catch (Exception ex) {
            throw new IllegalStateException("get failed: " + ex.getMessage(), ex);
        }
    }

    /**
     * Delete an object.
     *
     * @param bucket bucket
     * @param key    object key
     * @return true when the delete call succeeded
     */
    @Override
    public boolean delete(String bucket, String key) {
        try {
            client.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketOf(bucket))
                    .object(key)
                    .build());

            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Whether an object exists.
     *
     * @param bucket bucket
     * @param key    object key
     * @return true when present
     */
    @Override
    public boolean exists(String bucket, String key) {
        try {
            client.statObject(StatObjectArgs.builder()
                    .bucket(bucketOf(bucket))
                    .object(key)
                    .build());

            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * List object keys under a prefix.
     *
     * @param bucket bucket
     * @param prefix key prefix, nullable for all
     * @return object keys
     */
    @Override
    public List<String> list(String bucket, String prefix) {
        List<String> keys = new ArrayList<>();

        Iterable<io.minio.Result<io.minio.messages.Item>> results = client.listObjects(
                io.minio.ListObjectsArgs.builder()
                        .bucket(bucketOf(bucket))
                        .prefix(prefix)
                        .recursive(true)
                        .build());

        for (var result : results) {
            try {
                keys.add(result.get().objectName());
            } catch (Exception ex) {
                throw new IllegalStateException("list failed: " + ex.getMessage(), ex);
            }
        }

        return keys;
    }

    /**
     * List object metadata under a prefix in a single MinIO listing call —
     * size / last-modified come from each item, so no per-row stat round-trip.
     *
     * @param bucket bucket
     * @param prefix key prefix, nullable for all
     * @return object metadata (content-type is null: the listing does not carry it)
     */
    @Override
    public List<ObjectStat> listStat(String bucket, String prefix) {
        String bucketName = bucketOf(bucket);
        List<ObjectStat> rows = new ArrayList<>();

        try {
            for (var result : client.listObjects(io.minio.ListObjectsArgs.builder()
                    .bucket(bucketName)
                    .prefix(prefix)
                    .recursive(true)
                    .build())) {

                io.minio.messages.Item item = result.get();

                if (item.isDir()) {
                    continue;
                }

                LocalDateTime modified = LocalDateTime.ofInstant(item.lastModified().toInstant(),
                        java.time.ZoneId.systemDefault());

                rows.add(new ObjectStat(bucketName, item.objectName(), item.size(), modified, null));
            }

            return rows;
        } catch (Exception ex) {
            throw new IllegalStateException("list stat failed: " + ex.getMessage(), ex);
        }
    }

    /**
     * Object metadata from MinIO stat.
     *
     * @param bucket bucket
     * @param key    object key
     * @return stat, null when the object is missing
     */
    @Override
    public ObjectStat stat(String bucket, String key) {
        try {
            var response = client.statObject(StatObjectArgs.builder()
                    .bucket(bucketOf(bucket))
                    .object(key)
                    .build());

            return new ObjectStat(bucketOf(bucket), key, response.size(),
                    LocalDateTime.ofInstant(response.lastModified().toInstant(), java.time.ZoneId.systemDefault()),
                    response.contentType());
        } catch (ErrorResponseException ex) {
            if ("NoSuchKey".equals(ex.errorResponse().code())) {
                return null;
            }

            throw new IllegalStateException("stat failed: " + ex.errorResponse().code(), ex);
        } catch (Exception ex) {
            throw new IllegalStateException("stat failed: " + ex.getMessage(), ex);
        }
    }

    /**
     * Whether a bucket exists.
     *
     * @param bucket bucket name
     * @return true when present
     */
    @Override
    public boolean bucketExists(String bucket) {
        try {
            return client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        } catch (Exception ex) {
            throw new IllegalStateException("bucket exists failed: " + ex.getMessage(), ex);
        }
    }

    /**
     * Create a bucket (existing is a no-op).
     *
     * @param bucket bucket name
     */
    @Override
    public void createBucket(String bucket) {
        try {
            if (!bucketExists(bucket)) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
        } catch (Exception ex) {
            throw new IllegalStateException("create bucket failed: " + ex.getMessage(), ex);
        }
    }

    /**
     * Delete an EMPTY bucket; a non-empty bucket is a loud failure.
     *
     * @param bucket bucket name
     * @return true when the bucket is absent after the call, false when absent
     */
    @Override
    public boolean deleteBucket(String bucket) {
        try {
            if (!bucketExists(bucket)) {
                return true;
            }

            client.removeBucket(RemoveBucketArgs.builder().bucket(bucket).build());

            return true;
        } catch (ErrorResponseException ex) {
            if ("BucketNotEmpty".equals(ex.errorResponse().code())) {
                throw new IllegalStateException("bucket not empty: " + bucket);
            }

            throw new IllegalStateException("delete bucket failed: " + ex.errorResponse().code(), ex);
        } catch (Exception ex) {
            throw new IllegalStateException("delete bucket failed: " + ex.getMessage(), ex);
        }
    }

    /**
     * List all buckets with their creation time.
     *
     * @return bucket read models
     */
    @Override
    public List<BucketInfo> listBuckets() {
        try {
            return client.listBuckets(ListBucketsArgs.builder().build())
                    .stream()
                    .map(bucket -> new BucketInfo(bucket.name(), bucket.creationDate() == null ? null
                            : LocalDateTime.ofInstant(bucket.creationDate().toInstant(),
                                    java.time.ZoneId.systemDefault())))
                    .toList();
        } catch (Exception ex) {
            throw new IllegalStateException("list buckets failed: " + ex.getMessage(), ex);
        }
    }

    /**
     * Probe the MinIO / S3 endpoint with a bucket list; the bucket count and
     * the endpoint are reported back.
     *
     * @return probe result (failures reported in the payload, never thrown)
     */
    @Override
    public StorageProbe testConnection() {
        try {
            return StorageProbe.remote(true, listBuckets().size(), config.endpoint(), "ok");
        } catch (Exception ex) {
            return StorageProbe.remote(false, 0, config.endpoint(), "s3 connection failed " + ex.getMessage());
        }
    }

    /**
     * Resolve the bucket: blank falls back to the configured default bucket.
     *
     * @param bucket requested bucket
     * @return effective bucket name
     */
    private String bucketOf(String bucket) {
        return bucket == null || bucket.isBlank() ? defaultBucket() : bucket;
    }
}
