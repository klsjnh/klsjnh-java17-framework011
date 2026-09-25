package com.klsjnh.domain.platform011.export;

/*                ExportColumn class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  export column class
 *
 */

/**
 * Export column value object: the ordered presentation of one business field
 * inside an export payload.
 * <p>
 * The code is the database / row key (matching the row map key) and the name
 * is the human-readable header the front end renders. Immutable, compared by
 * value.
 * </p>
 *
 * @param code column code, the key used in every row map
 * @param name column display name, the header the front end shows
 */

public record ExportColumn(String code, String name) {
}
