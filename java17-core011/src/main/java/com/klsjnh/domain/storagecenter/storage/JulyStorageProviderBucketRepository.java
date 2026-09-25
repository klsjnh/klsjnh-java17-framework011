package com.klsjnh.domain.storagecenter.storage;

/*                JulyStorageProviderBucketRepository interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july storage provider bucket repository interface
 *
 */

import java.util.List;

/**
 * Repository port for the JulyStorageProviderBucket child entity: bucket reads
 * by master, the default bucket lookup and the per-master uniqueness support.
 */

public interface JulyStorageProviderBucketRepository {

    /**
     * Insert a new bucket.
     *
     * @param bucket entity
     */
    void insert(JulyStorageProviderBucket bucket);

    /**
     * Update an existing bucket.
     *
     * @param bucket entity with id
     */
    void update(JulyStorageProviderBucket bucket);

    /**
     * Find by primary key.
     *
     * @param id primary key
     * @return entity or null
     */
    JulyStorageProviderBucket findById(String id);

    /**
     * Logic delete by primary key.
     *
     * @param id primary key
     * @return true when a row was deleted
     */
    boolean logicDeleteById(String id);

    /**
     * Find the buckets of a storage instance, ordered by sort order.
     *
     * @param pkMt storage provider id
     * @return ordered entities, never null
     */
    List<JulyStorageProviderBucket> findByPkMt(String pkMt);

    /**
     * Find the default ENABLED bucket of a storage instance.
     *
     * @param pkMt storage provider id
     * @return entity or null when none
     */
    JulyStorageProviderBucket findDefault(String pkMt);

    /**
     * Whether a bucket code exists in an instance at all, INCLUDING
     * logic-deleted rows.
     *
     * @param pkMt       storage provider id
     * @param bucketCode bucket code
     * @return true when a row exists, deleted or not
     */
    boolean existsIncludingDeleted(String pkMt, String bucketCode);

    /**
     * Whether an instance has ANY bucket row at all, INCLUDING logic-deleted
     * rows — the seed decision.
     *
     * @param pkMt storage provider id
     * @return true when a row exists, deleted or not
     */
    boolean hasAnyIncludingDeleted(String pkMt);
}
