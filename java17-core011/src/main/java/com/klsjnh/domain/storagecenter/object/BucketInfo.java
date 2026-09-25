package com.klsjnh.domain.storagecenter.object;

/*                BucketInfo record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  bucket read model (name + creation date)
 *
 */

import java.time.LocalDateTime;

/**
 * Bucket read model (list result): the bucket name plus its creation time,
 * mirroring the legacy {@code listBuckets} payload.
 *
 * @param bucketName   bucket name
 * @param creationDate creation time, adapter-dependent and nullable
 */

public record BucketInfo(String bucketName, LocalDateTime creationDate) {
}
