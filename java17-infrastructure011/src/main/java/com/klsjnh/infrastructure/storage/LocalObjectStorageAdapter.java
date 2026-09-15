package com.klsjnh.infrastructure.storage;

/*                LocalObjectStorageAdapter class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  local object storage adapter class
 *
 */

import com.klsjnh.domain.storage.ObjectStat;
import com.klsjnh.domain.storage.ObjectStoragePort;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Stream;

/**
 * Local011 adapter: objects as files under the configured base path
 * (bucket = subdirectory). Active when default-type = local011 (default).
 */

@Component
@ConditionalOnProperty(name = "krt.storage-center.default-type", havingValue = "local011", matchIfMissing = true)
public class LocalObjectStorageAdapter implements ObjectStoragePort {

    /**
     * Storage properties.
     */
    private final StorageProperties properties;

    /**
     * Create the adapter.
     *
     * @param properties storage properties
     */
    public LocalObjectStorageAdapter(StorageProperties properties) {
        this.properties = properties;
    }

    /**
     * Store an object as a file.
     *
     * @param bucket      bucket (subdirectory)
     * @param key         object key
     * @param content     object bytes
     * @param contentType mime type, ignored by the local adapter
     * @return the final stored key
     */
    @Override
    public String put(String bucket, String key, byte[] content, String contentType) {
        String funcName = "put";

        try {
            Path path = resolve(bucket, key);
            Files.createDirectories(path.getParent());
            Files.write(path, content);

            return key;
        } catch (IOException ex) {
            throw new IllegalStateException(funcName + " failed: " + ex.getMessage(), ex);
        }
    }

    /**
     * Read an object file.
     *
     * @param bucket bucket
     * @param key    object key
     * @return object bytes, null when missing
     */
    @Override
    public byte[] get(String bucket, String key) {
        try {
            Path path = resolve(bucket, key);

            return Files.exists(path) ? Files.readAllBytes(path) : null;
        } catch (IOException ex) {
            throw new IllegalStateException("get failed: " + ex.getMessage(), ex);
        }
    }

    /**
     * Delete an object file (idempotent: absent already counts as deleted).
     *
     * @param bucket bucket
     * @param key    object key
     * @return true when the object is absent after the call
     */
    @Override
    public boolean delete(String bucket, String key) {
        try {
            Path path = resolve(bucket, key);
            Files.deleteIfExists(path);
            pruneEmptyParents(path.getParent(), Paths.get(properties.getLocal011().getBasePath(), safe(bucket)));

            return true;
        } catch (IOException ex) {
            throw new IllegalStateException("delete failed: " + ex.getMessage(), ex);
        }
    }

    /**
     * Prune now-empty parent directories up to (not including) the bucket
     * root — best effort, mirroring object-storage semantics where removing
     * the last object empties its virtual folder.
     *
     * @param dir  starting directory
     * @param stop the bucket root (never pruned)
     */
    private void pruneEmptyParents(Path dir, Path stop) {
        try {
            while (dir != null && dir.startsWith(stop) && !dir.equals(stop)) {
                try (Stream<Path> children = Files.list(dir)) {
                    if (children.findAny().isPresent()) {
                        return;
                    }
                }

                Files.delete(dir);
                dir = dir.getParent();
            }
        } catch (IOException ignored) {
            // best effort pruning — a leftover empty dir never breaks deletes
        }
    }

    /**
     * Whether an object file exists.
     *
     * @param bucket bucket
     * @param key    object key
     * @return true when present
     */
    @Override
    public boolean exists(String bucket, String key) {
        return Files.exists(resolve(bucket, key));
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
        Path dir = Paths.get(properties.getLocal011().getBasePath(), safe(bucket));

        if (!Files.exists(dir)) {
            return List.of();
        }

        try (Stream<Path> walk = Files.walk(dir)) {
            return walk.filter(Files::isRegularFile)
                    .map(p -> dir.relativize(p).toString().replace('\\', '/'))
                    .filter(k -> prefix == null || prefix.isBlank() || k.startsWith(prefix))
                    .sorted()
                    .toList();
        } catch (IOException ex) {
            throw new IllegalStateException("list failed: " + ex.getMessage(), ex);
        }
    }

    /**
     * Resolve the file path of an object inside the base path; rejects
     * traversal keys.
     *
     * @param bucket bucket
     * @param key    object key
     * @return absolute path
     */
    private Path resolve(String bucket, String key) {
        if (key == null || key.contains("..")) {
            throw new IllegalStateException("invalid object key");
        }

        return Paths.get(properties.getLocal011().getBasePath(), safe(bucket), key);
    }

    /**
     * Object metadata: size / last-modified from the file, content-type
     * inferred from the file extension.
     *
     * @param bucket bucket
     * @param key    object key
     * @return stat, null when the object is missing
     */
    @Override
    public ObjectStat stat(String bucket, String key) {
        Path path = resolve(bucket, key);

        if (!Files.exists(path)) {
            return null;
        }

        try {
            long size = Files.size(path);
            LocalDateTime modified = LocalDateTime.ofInstant(Files.getLastModifiedTime(path).toInstant(),
                    ZoneId.systemDefault());

            return new ObjectStat(safe(bucket), key, size, modified, Files.probeContentType(path));
        } catch (IOException ex) {
            throw new IllegalStateException("stat failed: " + ex.getMessage(), ex);
        }
    }

    /**
     * Whether a bucket directory exists.
     *
     * @param bucket bucket name
     * @return true when present
     */
    @Override
    public boolean bucketExists(String bucket) {
        return Files.isDirectory(Paths.get(properties.getLocal011().getBasePath(), safe(bucket)));
    }

    /**
     * Create a bucket directory (existing is a no-op).
     *
     * @param bucket bucket name
     */
    @Override
    public void createBucket(String bucket) {
        try {
            Files.createDirectories(Paths.get(properties.getLocal011().getBasePath(), safe(bucket)));
        } catch (IOException ex) {
            throw new IllegalStateException("create bucket failed: " + ex.getMessage(), ex);
        }
    }

    /**
     * Delete an EMPTY bucket directory; a non-empty bucket is a loud failure.
     *
     * @param bucket bucket name
     * @return true when the bucket is absent after the call, false when absent
     */
    @Override
    public boolean deleteBucket(String bucket) {
        Path dir = Paths.get(properties.getLocal011().getBasePath(), safe(bucket));

        if (!Files.isDirectory(dir)) {
            return true;
        }

        try {
            Files.delete(dir);

            return true;
        } catch (DirectoryNotEmptyException ex) {
            throw new IllegalStateException("bucket not empty: " + safe(bucket));
        } catch (IOException ex) {
            throw new IllegalStateException("delete bucket failed: " + ex.getMessage(), ex);
        }
    }

    /**
     * List bucket directories under the base path.
     *
     * @return bucket names
     */
    @Override
    public List<String> listBuckets() {
        Path base = Paths.get(properties.getLocal011().getBasePath());

        if (!Files.isDirectory(base)) {
            return List.of();
        }

        try (Stream<Path> list = Files.list(base)) {
            return list.filter(Files::isDirectory)
                    .map(p -> p.getFileName().toString())
                    .sorted()
                    .toList();
        } catch (IOException ex) {
            throw new IllegalStateException("list buckets failed: " + ex.getMessage(), ex);
        }
    }

    /**
     * The storage center's default bucket: the shared top-level value.
     *
     * @return default bucket name
     */
    @Override
    public String defaultBucket() {
        return properties.getDefaultBucket();
    }

    /**
     * Normalize a bucket argument: blank falls back to the default bucket,
     * then the name must be a single safe path segment.
     *
     * @param bucket bucket argument, nullable
     * @return safe bucket name
     */
    private String safe(String bucket) {
        String name = bucket == null || bucket.isBlank() ? defaultBucket() : bucket;

        if (name == null || name.isBlank()
                || name.contains("..") || name.contains("/") || name.contains("\\")) {
            throw new IllegalStateException("invalid bucket name");
        }

        return name;
    }
}
