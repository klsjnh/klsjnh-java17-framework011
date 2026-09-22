package com.klsjnh.domain.platform011.importdata;

/*                ImportSheet class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.22  import sheet class
 *
 */

import java.util.List;
import java.util.Map;

/**
 * One decoded sheet from an import file.
 *
 * @param name sheet name
 * @param rows row maps (keys = header codes); 1-based Excel row index is not
 *             stored here — providers report errors with sheet + logical index
 */

public record ImportSheet(String name, List<Map<String, Object>> rows) {

    /**
     * Compact constructor: defensive copy.
     *
     * @param name sheet name
     * @param rows rows
     */
    public ImportSheet {
        rows = rows == null ? List.of() : List.copyOf(rows);
    }
}
