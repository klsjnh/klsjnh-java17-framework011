package com.klsjnh.domain.platform011.export;

/*                ExportSheetSpec class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.22  export sheet spec class
 *
 */

import java.util.List;

/**
 * Declares one sheet a provider can export or import: name + column layout.
 *
 * @param name    sheet name (master / children)
 * @param columns ordered columns (header codes)
 */

public record ExportSheetSpec(String name, List<ExportColumn> columns) {

    /**
     * Compact constructor: defensive copy.
     *
     * @param name    sheet name
     * @param columns columns
     */
    public ExportSheetSpec {
        columns = columns == null ? List.of() : List.copyOf(columns);
    }
}
