package com.klsjnh.domain.storagecenter.object;

/*                ObjectStorageProviderFactory interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  pluggable storage provider SPI
 *
 */

import java.util.List;

/**
 * SPI: a storage vendor plug-in. Implementations are collected by the
 * storage-center registry (Spring {@code List} injection), so a new provider
 * (e.g. oss011 / cos011) can be added by a consumer without touching the
 * platform. Provider codes are an open string vocabulary, never an enum that
 * would have to be extended inside the platform.
 */

public interface ObjectStorageProviderFactory {

    /**
     * Provider codes this factory serves (open string vocabulary).
     *
     * @return provider codes, never null
     */
    List<String> providers();

    /**
     * Create an adapter for the given connection config.
     *
     * @param config connection config
     * @return storage adapter, never null
     */
    ObjectStoragePort create(StorageConnectionConfig config);
}
