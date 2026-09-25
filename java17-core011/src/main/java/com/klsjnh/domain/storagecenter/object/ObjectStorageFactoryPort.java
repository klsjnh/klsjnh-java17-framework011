package com.klsjnh.domain.storagecenter.object;


import com.klsjnh.domain.storagecenter.object.StorageConnectionConfig;

/*                ObjectStorageFactoryPort interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  object storage factory port interface
 *
 */

/**
 * Builds an {@link ObjectStoragePort} adapter from a connection config
 * (provider-driven), so the runtime can host many storage instances.
 */

public interface ObjectStorageFactoryPort {

    /**
     * Create an adapter for the given connection config.
     *
     * @param config connection config
     * @return storage adapter, never null
     */
    ObjectStoragePort create(StorageConnectionConfig config);
}
