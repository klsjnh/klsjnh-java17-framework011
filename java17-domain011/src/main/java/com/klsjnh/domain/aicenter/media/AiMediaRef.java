package com.klsjnh.domain.aicenter.media;

/*                AiMediaRef record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  ai media reference record
 *      2026.09.24  return full locator (storageCode / bucket / objectKey)
 *
 */

/**
 * A stored media reference: where the artifact lives and how to reach it. Aligns
 * with the storage-center object-metadata convention (storageCode + bucket +
 * objectKey). The consumer owns its lifecycle.
 *
 * @param storageCode storage instance code used for the write
 * @param bucket      physical bucket name used for the write
 * @param objectKey   storage object key (bucket-unique), nullable when the
 *                    artifact was already a remote url
 * @param url         access url (presigned for S3 / MinIO, file uri for local)
 * @param mimeType    media mime type, nullable
 * @param size        byte size, 0 when unknown
 */

public record AiMediaRef(String storageCode, String bucket, String objectKey, String url, String mimeType, long size) {

    /**
     * Alias of {@link #objectKey()} for callers that still say "storage key".
     *
     * @return object key, nullable
     */
    public String storageKey() {
        return objectKey;
    }
}
