package com.klsjnh.domain.platform011.export;

/*                ExportResult class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  export result class
 *
 */

import java.util.List;
import java.util.Map;

/**
 * Export result value object: the complete exported payload handed to any
 * consumer, carrying no transport concern of its own.
 * <p>
 * Part one is the metadata (object info + column info), part two is the row
 * data. Rows are column-keyed ordered maps — the keys are exactly the codes
 * carried by {@link ExportMetaInfo#columns()}.
 * </p>
 * <p>
 * Rows come from the provider's live query, so deleted rows ({@code dr = '1'})
 * are excluded: the persistence layer is not bypassed to reach tombstones.
 * </p>
 * <p>
 * This type deliberately carries data only. Whether it ends up in an HTTP
 * response or in the storage center is decided by the caller, so the same
 * result serves both the download and the backup path.
 * </p>
 *
 * @param metaInfo object info + column info + row count
 * @param rows     full row data
 */

public record ExportResult(ExportMetaInfo metaInfo, List<Map<String, Object>> rows) {

    /**
     * Compact constructor: defensively copy the row list so the record stays
     * immutable.
     *
     * @param metaInfo object and column information
     * @param rows     full row data
     */
    public ExportResult {
        rows = rows == null ? List.of() : List.copyOf(rows);
    }

    /**
     * Build a result, deriving the row count from the row list.
     *
     * @param objectCode object code
     * @param columns    ordered column definitions
     * @param rows       full row data
     * @return export result
     */
    public static ExportResult of(String objectCode, List<ExportColumn> columns, List<Map<String, Object>> rows) {
        List<Map<String, Object>> safeRows = rows == null ? List.of() : rows;

        return new ExportResult(ExportMetaInfo.of(objectCode, columns, safeRows.size()), safeRows);
    }
}
