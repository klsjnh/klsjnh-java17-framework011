package com.klsjnh.application.storagecenter;

/*                StorageBucketExists class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  bucket existence read model (getBucket payload)
 *
 */

/**
 * Bucket existence read model ({@code getBucket} payload). A successful call
 * always means the bucket exists — a missing bucket is reported as 404.
 *
 * @param bucketName bucket name
 * @param exists     true (a missing bucket never reaches this read model)
 */

public record StorageBucketExists(String bucketName, boolean exists) {
}
