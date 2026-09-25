package com.klsjnh.domain.aicenter.media;

/*                AiMediaLocation record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.24
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.24  call-time storage locator for AI media
 *
 */

/**
 * Call-time storage locator for AI media persistence. The caller must supply a
 * storage instance ({@code storageCode} preferred, or {@code storageId}) and a
 * bucket ({@code bucketCode} preferred, or {@code bucketId}). There is no
 * framework default — missing pairs are a parameter error.
 *
 * @param storageCode storage instance code ({@code july_storage_provider.storage_code})
 * @param storageId   storage instance id, alternative to storageCode
 * @param bucketCode  bucket code within the instance
 * @param bucketId    bucket id, alternative to bucketCode
 */

public record AiMediaLocation(String storageCode, String storageId, String bucketCode, String bucketId) {
}
