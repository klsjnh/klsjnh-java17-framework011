package com.klsjnh.application.storage011;

/*                StorageBucketUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  storage bucket use case class
 *
 */

import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;

import com.klsjnh.domain.storage.ObjectStoragePort;
import com.klsjnh.domain.storage.StorageResolverPort;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Storage bucket use cases: list / get / create / remove / probe, resolved per
 * storage code.
 */

@Service
public class StorageBucketUseCase {

    /**
     * Storage resolver.
     */
    private final StorageResolverPort resolver;

    /**
     * Create the use case.
     *
     * @param resolver storage resolver
     */
    public StorageBucketUseCase(StorageResolverPort resolver) {
        this.resolver = resolver;
    }

    /**
     * List bucket names.
     *
     * @param storageCode storage code, blank for the default instance
     * @return bucket names, never null
     */
    public List<String> selectList(String storageCode) {
        return adapter(storageCode).listBuckets();
    }

    /**
     * Page bucket names.
     *
     * @param storageCode storage code
     * @param pageIndex   page index, 1 based
     * @param pageSize    page size
     * @param keyword     name keyword, nullable
     * @return page result
     */
    public PageResult011<String> selectListByPage(String storageCode, Integer pageIndex, Integer pageSize,
            String keyword) {
        PageQuery011 query = new PageQuery011(pageIndex, pageSize);
        List<String> all = adapter(storageCode).listBuckets().stream()
                .filter(name -> keyword == null || keyword.isBlank() || name.contains(keyword.trim()))
                .toList();

        int from = (int) Math.min(query.offset(), all.size());
        int to = (int) Math.min((long) from + query.pageSize(), all.size());

        return PageResult011.of(query, all.size(), all.subList(from, to));
    }

    /**
     * Whether a bucket exists.
     *
     * @param storageCode storage code
     * @param bucketName  bucket name
     * @return true when present
     */
    public boolean exists(String storageCode, String bucketName) {
        return adapter(storageCode).bucketExists(bucketName);
    }

    /**
     * Create a bucket (existing is a no-op).
     *
     * @param storageCode storage code
     * @param bucketName  bucket name
     * @param region      region, ignored by local / MinIO
     */
    public void insert(String storageCode, String bucketName, String region) {
        adapter(storageCode).createBucket(bucketName);
    }

    /**
     * Remove an EMPTY bucket.
     *
     * @param storageCode storage code
     * @param bucketName  bucket name
     */
    public void remove(String storageCode, String bucketName) {
        adapter(storageCode).deleteBucket(bucketName);
    }

    /**
     * Probe the resolved instance.
     *
     * @param storageCode storage code
     * @return probe result, never null
     */
    public StorageProbeResult testConnection(String storageCode) {
        try {
            adapter(storageCode).listBuckets();

            return StorageProbeResult.ok("connected");
        } catch (Exception ex) {
            return StorageProbeResult.fail(ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
    }

    /**
     * Resolve the adapter, translating resolution failures into 400.
     *
     * @param storageCode storage code
     * @return adapter
     */
    private ObjectStoragePort adapter(String storageCode) {
        try {
            return resolver.resolve(storageCode);
        } catch (IllegalStateException ex) {
            throw BusinessException.badRequest(ex.getMessage());
        }
    }
}
