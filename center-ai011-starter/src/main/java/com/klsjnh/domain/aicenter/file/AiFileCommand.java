package com.klsjnh.domain.aicenter.file;

/*                AiFileCommand record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.22  provider file lookup command
 *
 */

/**
 * One provider file lookup (get / delete / content).
 *
 * @param baseUrl      provider base url
 * @param apiKey       api key secret
 * @param providerCode provider code
 * @param fileId       provider file id
 */

public record AiFileCommand(String baseUrl, String apiKey, String providerCode, String fileId) {
}
