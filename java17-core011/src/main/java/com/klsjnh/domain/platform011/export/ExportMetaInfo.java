package com.klsjnh.domain.platform011.export;

/*                ExportMetaInfo class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  export meta info class
 *      2026.09.15  clock from date util 011
 *
 */

import com.klsjnh.common.util.DateUtil011;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Export metadata value object: everything the front end needs to render the
 * payload header, plus the row count actually exported.
 * <p>
 * Columns are ordered — the front end renders them in the given order and
 * every row map carries exactly these codes as keys.
 * </p>
 *
 * @param objectCode object code (e.g. julyUser)
 * @param exportTime export time
 * @param rowCount   number of rows in the payload
 * @param columns    ordered column definitions
 */

public record ExportMetaInfo(String objectCode, LocalDateTime exportTime, int rowCount, List<ExportColumn> columns) {

    /**
     * Compact constructor: defensively copy the column list so the record
     * stays immutable.
     *
     * @param objectCode object code
     * @param exportTime export time
     * @param rowCount   number of rows
     * @param columns    ordered column definitions
     */
    public ExportMetaInfo {
        columns = columns == null ? List.of() : List.copyOf(columns);
    }

    /**
     * Build metadata with the current time and the row count derived from the
     * given row list.
     *
     * @param objectCode object code
     * @param columns    ordered column definitions
     * @param rowCount   number of rows in the payload
     * @return metadata
     */
    public static ExportMetaInfo of(String objectCode, List<ExportColumn> columns, int rowCount) {
        return new ExportMetaInfo(objectCode, DateUtil011.now(), rowCount, columns);
    }
}
