package com.klsjnh.domain.platform011.export;

/*                ExportWorkbook class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.22  export workbook class
 *
 */

import java.util.List;

/**
 * Multi-sheet export payload for xlsx (and any future multi-sheet format).
 *
 * @param objectCode object code
 * @param sheets     ordered sheets
 */

public record ExportWorkbook(String objectCode, List<ExportSheet> sheets) {

    /**
     * Compact constructor: defensive copy.
     *
     * @param objectCode object code
     * @param sheets     sheets
     */
    public ExportWorkbook {
        sheets = sheets == null ? List.of() : List.copyOf(sheets);
    }
}
