package com.klsjnh.domain.platform011.importdata;

/*                ImportRowError class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.22  import row error class
 *
 */

/**
 * One row-level import failure.
 *
 * @param sheet   sheet name
 * @param rowIndex 1-based data row index within the sheet (header is row 1)
 * @param message error message
 */

public record ImportRowError(String sheet, int rowIndex, String message) {
}
