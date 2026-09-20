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
 *
 */

/**
 * A stored media reference: where the artifact lives and how to reach it. The
 * consumer owns its lifecycle.
 *
 * @param storageKey storage object key, nullable when the artifact was already
 *                   a remote url
 * @param url        access url (presigned for S3 / MinIO, file uri for local)
 * @param mimeType   media mime type, nullable
 * @param size       byte size, 0 when unknown
 */

public record AiMediaRef(String storageKey, String url, String mimeType, long size) {
}
