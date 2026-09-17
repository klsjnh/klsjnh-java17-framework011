package com.klsjnh.domain.lowcode011;

/*                MetadataDataWriterPort interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  metadata data writer port
 *
 */

import java.util.List;
import java.util.Map;

/**
 * Data writer port: upserts source rows into a generated physical table.
 * Implementations only ever write columns that exist on the target table and
 * must bind values (never interpolate them).
 */

public interface MetadataDataWriterPort {

    /**
     * Upsert rows into a physical table (by primary key; insert-or-update). When
     * the source carries no primary key, the id is derived deterministically
     * from {@code businessField} so re-importing the same page updates instead
     * of duplicating.
     *
     * @param physicalTable target table
     * @param businessField business field code (source column matched case-insensitively)
     * @param rows          source rows (column label → value)
     * @return number of rows processed
     */
    int upsert(String physicalTable, String businessField, List<Map<String, Object>> rows);
}
