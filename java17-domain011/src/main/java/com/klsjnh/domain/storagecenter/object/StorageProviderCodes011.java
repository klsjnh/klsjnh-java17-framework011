package com.klsjnh.domain.storagecenter.object;


/*                StorageProviderCodes011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  built-in storage provider codes
 *
 */

/**
 * Built-in storage provider codes. These are plain string constants, not an
 * enum: consumers may register any other code through
 * {@link ObjectStorageProviderFactory} without extending the platform.
 */

public final class StorageProviderCodes011 {

    /** Local disk adapter. */
    public static final String LOCAL = "local011";

    /** Self-hosted MinIO adapter. */
    public static final String MINIO = "minio011";

    /** S3-compatible adapter. */
    public static final String S3 = "s3011";

    private StorageProviderCodes011() {
    }
}
