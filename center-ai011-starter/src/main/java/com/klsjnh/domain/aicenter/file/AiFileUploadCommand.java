package com.klsjnh.domain.aicenter.file;

/*                AiFileUploadCommand record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.22  provider file upload command
 *
 */

/**
 * One provider file upload: either bytes or a remote URL (bytes win when both
 * are given).
 *
 * @param baseUrl      provider base url
 * @param apiKey       api key secret, never echoed
 * @param providerCode provider code
 * @param fileName     file name
 * @param bytes        file bytes, nullable when url is used
 * @param url          remote file url, nullable when bytes are used
 * @param purpose      neutral purpose
 */

public record AiFileUploadCommand(String baseUrl, String apiKey, String providerCode, String fileName, byte[] bytes,
        String url, AiFilePurpose purpose) {
}
