package com.klsjnh.domain.lowcode011;

/*                JulyMetadataOpenApi record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  open api config record
 *
 */

import java.time.LocalDateTime;

/**
 * Open API configuration of a low-code object: whether the object is exposed,
 * the auth mode (closed / none / apiKey), the allowed operations and the api
 * key. The key is masked on output; rotation returns it in clear exactly once.
 *
 * @param id              primary key
 * @param objectName      low-code object name
 * @param enabled         whether the open API is enabled
 * @param authMode        closed / none / apiKey
 * @param allowedOps      comma list of query/insert/update/delete
 * @param apiKey          api key (masked on output)
 * @param apiKeyUpdatedAt last rotation time, nullable
 */

public record JulyMetadataOpenApi(String id, String objectName, boolean enabled, String authMode, String allowedOps,
        String apiKey, LocalDateTime apiKeyUpdatedAt) {
}
