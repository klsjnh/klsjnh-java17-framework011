package com.klsjnh.infrastructure.storage;

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

import com.klsjnh.domain.storage.ObjectStat;
import com.klsjnh.domain.storage.ObjectStoragePort;

import io.minio.messages.Bucket;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.ListBucketsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveBucketArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

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
@Component
@ConditionalOnProperty(name = "krt.storage-center.default-type", havingValue = "minio011")
public class MinioObjectStorageAdapter implements ObjectStoragePort {

    /**
     * MinIO client.
     */
    private final MinioClient client;

    /**
     * Storage properties.
     */
    private final StorageProperties properties;

    /**
     * Create the adapter and ensure the default bucket exists.
     *
     * @param properties storage properties
     */
    public MinioObjectStorageAdapter(StorageProperties properties) {
        this.properties = properties;
        StorageProperties.Minio011 minio = properties.getMinio011();

        this.client = MinioClient.builder()
                .endpoint(minio.getEndpoint())
                .credentials(minio.getAccessKey(), minio.getSecretKey())
                .build();

        try {
            boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(minio.getDefaultBucket()).build());

            if (!exists) {
                client.makeBucket(MakeBucketArgs.builder().bucket(minio.getDefaultBucket()).build());
                log.info("minio011 bucket {} created ...", minio.getDefaultBucket());
            }
        } catch (Exception ex) {
            log.warn("minio011 bucket ensure failed {} ...", ex.getMessage());
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
     * List all bucket names.
     *
     * @return bucket names
     */
    @Override
    public List<String> listBuckets() {
        try {
            return client.listBuckets(ListBucketsArgs.builder().build())
                    .stream()
                    .map(Bucket::name)
                    .toList();
        } catch (Exception ex) {
            throw new IllegalStateException("list buckets failed: " + ex.getMessage(), ex);
        }
    }

    /**
     * Resolve the bucket: blank falls back to the configured default bucket.
     *
     * @param bucket requested bucket
     * @return effective bucket name
     */
    private String bucketOf(String bucket) {
        return bucket == null || bucket.isBlank()
                ? properties.getMinio011().getDefaultBucket()
                : bucket;
    }
}
