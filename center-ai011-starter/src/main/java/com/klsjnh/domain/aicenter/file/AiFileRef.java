package com.klsjnh.domain.aicenter.file;

/*                AiFileRef record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.22  provider file asset reference
 *
 */

/**
 * One provider-side file asset (metadata only; the bytes live at the vendor).
 *
 * @param providerCode provider code
 * @param fileId       provider file id
 * @param fileName     original file name, nullable
 * @param bytes        size in bytes
 * @param purpose      wire purpose
 * @param status       provider status (processed / uploading ...), nullable
 * @param createdAt    provider creation epoch seconds, nullable
 */

public record AiFileRef(String providerCode, String fileId, String fileName, long bytes, String purpose,
        String status, Long createdAt) {
}
