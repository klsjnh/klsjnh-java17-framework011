package com.klsjnh.domain.platform011.export;

/*                ExportSheet class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.22  export sheet class
 *
 */

import java.util.List;
import java.util.Map;

/**
 * One sheet of an export workbook: name, ordered columns and row maps.
 *
 * @param name    sheet name (e.g. master / children)
 * @param columns ordered column definitions
 * @param rows    row maps keyed by column code
 */

public record ExportSheet(String name, List<ExportColumn> columns, List<Map<String, Object>> rows) {

    /**
     * Compact constructor: defensive copies.
     *
     * @param name    sheet name
     * @param columns columns
     * @param rows    rows
     */
    public ExportSheet {
        columns = columns == null ? List.of() : List.copyOf(columns);
        rows = rows == null ? List.of() : List.copyOf(rows);
    }
}
