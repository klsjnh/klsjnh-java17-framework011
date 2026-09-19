package com.klsjnh.domain.storagecenter.object;

/*                StorageDefaultsPort interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  storage center config defaults port
 *
 */

/**
 * Storage center defaults declared in configuration: the default bucket of the
 * active adapter ({@code local011.default-bucket} / {@code minio011.default-bucket}).
 * A blank value means "fall back to the resolved instance's own default bucket".
 */

public interface StorageDefaultsPort {

    /**
     * The default bucket of the active adapter ({@code krt.storage-center.default-type}).
     *
     * @return default bucket name, blank when unset
     */
    String defaultBucket();
}
