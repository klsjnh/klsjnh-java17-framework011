package com.klsjnh.application.storagecenter;

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

import com.klsjnh.domain.storagecenter.BucketInfo;
import com.klsjnh.domain.storagecenter.ObjectStoragePort;
import com.klsjnh.domain.storagecenter.StorageProbe;
import com.klsjnh.domain.storagecenter.StorageResolverPort;

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
     * List buckets (name + creation time).
     *
     * @param storageCode storage code, blank for the default instance
     * @return bucket read models, never null
     */
    public List<BucketInfo> selectList(String storageCode) {
        return adapter(storageCode).listBuckets();
    }

    /**
     * Page buckets (name + creation time), filtered by a name keyword.
     *
     * @param storageCode storage code
     * @param pageIndex   page index, 1 based
     * @param pageSize    page size
     * @param keyword     name keyword, nullable
     * @return page result of bucket read models
     */
    public PageResult011<BucketInfo> selectListByPage(String storageCode, Integer pageIndex, Integer pageSize,
            String keyword) {
        PageQuery011 query = new PageQuery011(pageIndex, pageSize);
        List<BucketInfo> all = adapter(storageCode).listBuckets().stream()
                .filter(info -> keyword == null || keyword.isBlank() || info.bucketName().contains(keyword.trim()))
                .toList();

        int from = (int) Math.min(query.offset(), all.size());
        int to = (int) Math.min((long) from + query.pageSize(), all.size());

        return PageResult011.of(query, all.size(), all.subList(from, to));
    }

    /**
     * Get a bucket; a missing bucket is a 404.
     *
     * @param storageCode storage code
     * @param bucketName  bucket name
     * @return bucket existence read model
     */
    public StorageBucketExists getBucket(String storageCode, String bucketName) {
        if (!adapter(storageCode).bucketExists(bucketName)) {
            throw BusinessException.recordNotFound(bucketName);
        }

        return new StorageBucketExists(bucketName, true);
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
    public StorageProbe testConnection(String storageCode) {
        return adapter(storageCode).testConnection();
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
