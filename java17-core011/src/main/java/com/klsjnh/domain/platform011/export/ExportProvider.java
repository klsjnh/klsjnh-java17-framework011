package com.klsjnh.domain.platform011.export;

/*                ExportProvider interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  export provider interface
 *      2026.09.15  moved to the domain layer, switched to offset paging
 *
 */

import java.util.List;
import java.util.Map;

/**
 * Export provider port: one implementation per exportable object, registered
 * as a bean and collected by the registry.
 * <p>
 * The platform drives the export in batches so a large table can never be
 * pulled in a single statement — each call fetches at most {@code limit} rows
 * starting at {@code offset}. Implementations MUST page at the database level
 * (a real LIMIT/OFFSET) rather than fetching everything and slicing in memory,
 * otherwise the batching protects nothing.
 * </p>
 * <p>
 * Rows are column-keyed ordered maps and MUST NOT include sensitive columns
 * (e.g. password hashes). Rows come from the live query — deleted rows are
 * excluded, since the persistence layer is not bypassed to reach them.
 * </p>
 */

public interface ExportProvider {

    /**
     * Get the unique object code this provider exports (e.g. julyUser).
     *
     * @return object code
     */
    String objectCode();

    /**
     * Get the ordered column definitions of this object.
     *
     * @return column definitions, ordered
     */
    List<ExportColumn> columns();

    /**
     * Fetch one batch of rows.
     *
     * @param offset row offset, 0 based
     * @param limit  max rows to return
     * @return rows, empty when the offset is past the end
     */
    List<Map<String, Object>> exportRows(int offset, int limit);

    /**
     * Sheet layout for multi-sheet formats (xlsx). Default: a single
     * {@code master} sheet using {@link #columns()}.
     *
     * @return sheet specs in order
     */
    default List<ExportSheetSpec> sheetSpecs() {
        return List.of(new ExportSheetSpec("master", columns()));
    }

    /**
     * Fetch one batch of rows for a named sheet. Default delegates
     * {@code master} to {@link #exportRows}; other names return empty.
     *
     * @param sheetName sheet name
     * @param offset    row offset, 0 based
     * @param limit     max rows
     * @return rows
     */
    default List<Map<String, Object>> exportSheetRows(String sheetName, int offset, int limit) {
        if ("master".equals(sheetName)) {
            return exportRows(offset, limit);
        }

        return List.of();
    }
}
