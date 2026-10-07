package com.klsjnh.domain.platform011.export;

/*                SheetRowSource interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.06
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.06  streaming row source (batch fetch function)
 *
 */

import java.util.List;
import java.util.Map;

/**
 * Streaming row source: fetches one batch of rows per call so a stream writer
 * can walk the whole data set with bounded memory (one batch at a time, never
 * the full result). Same contract as {@link ExportProvider#exportRows}.
 */

@FunctionalInterface
public interface SheetRowSource {

    /**
     * Fetch one batch of rows.
     *
     * @param offset row offset, 0 based
     * @param limit  max rows to return
     * @return rows, empty when the offset is past the end
     */
    List<Map<String, Object>> fetch(int offset, int limit);
}
