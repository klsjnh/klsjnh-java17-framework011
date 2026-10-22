package com.klsjnh.domain.aicenter.file;

/*                AiFileListCommand record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.22  provider file list command
 *
 */

/**
 * One provider file list query.
 *
 * @param baseUrl      provider base url
 * @param apiKey       api key secret
 * @param providerCode provider code
 * @param purpose      purpose filter, nullable for all
 */

public record AiFileListCommand(String baseUrl, String apiKey, String providerCode, AiFilePurpose purpose) {
}
