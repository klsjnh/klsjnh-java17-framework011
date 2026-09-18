package com.klsjnh.domain.lowcode011;

/*                JulyMetadataVersion record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  publish snapshot read model
 *
 */

import java.time.LocalDateTime;

/**
 * Immutable publish snapshot of a low-code object: the MetaDTO JSON captured at
 * publish time together with the physical table it produced. DRAFT edits never
 * touch an existing snapshot.
 *
 * @param id            primary key
 * @param objectName    low-code object name
 * @param version       version string (e.g. 0.0.1)
 * @param payloadJson   MetaDTO snapshot JSON
 * @param physicalTable produced physical table name
 * @param ddlText       DDL executed by this publish, nullable for legacy
 * @param publishStatus snapshot status (PUBLISHED; the flow is stateless, no PENDING)
 * @param publishedBy   publisher id, nullable
 * @param publishedAt   publish time, nullable
 */

public record JulyMetadataVersion(String id, String objectName, String version, String payloadJson,
        String physicalTable, String ddlText, String publishStatus, String publishedBy,
        LocalDateTime publishedAt) {
}
