package com.klsjnh.domain.storagecenter;

/*                StorageResolverPort interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  storage resolver port interface
 *
 */

/**
 * Resolves a storage code to the {@link ObjectStoragePort} adapter serving it
 * (the table-driven multi-instance entry). Implementations cache adapters.
 */

public interface StorageResolverPort {

    /**
     * Resolve the adapter for a storage code.
     *
     * @param storageCode storage code, may be blank for the default instance
     * @return storage adapter, never null
     */
    ObjectStoragePort resolve(String storageCode);

    /**
     * Evict the cached adapter of a storage code (after an instance edit).
     *
     * @param storageCode storage code
     */
    void evict(String storageCode);
}
