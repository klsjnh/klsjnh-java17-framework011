package com.klsjnh.domain.platform011.export;

/*                ExportStreamPort interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.06
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.06  streaming export port (csv / json / xlsx, batch-at-a-time)
 *
 */

import java.io.OutputStream;
import java.util.List;

/**
 * Port for streaming export payloads: rows are pulled through the
 * {@link SheetRowSource} batch by batch and written straight to the target
 * stream, so memory stays bounded at one batch regardless of the result size
 * (the byte[]-based {@link XlsxWorkbookPort#write} stays for the backup path).
 * <p>
 * Implementations live in infrastructure (POI SXSSF / RFC 4180 CSV / Jackson
 * streaming); the application never touches those types. Every method returns
 * the number of data rows written so the caller can audit after the stream
 * completes.
 * </p>
 */

public interface ExportStreamPort {

    /**
     * Stream the sheet as RFC 4180 CSV (UTF-8 with BOM, header = column
     * names). Single sheet only — multi-sheet objects use xlsx.
     *
     * @param sheetName sheet / file label (informational)
     * @param columns   ordered column definitions
     * @param source    batched row source
     * @param batchSize rows per fetch round trip
     * @param out       target stream (not closed by the implementation)
     * @return data rows written
     */
    long writeCsv(String sheetName, List<ExportColumn> columns, SheetRowSource source, int batchSize,
            OutputStream out);

    /**
     * Stream the sheet as a JSON object {@code {"objectCode":…,
     * "columns":[…], "rows":[…], "rowCount":…}} via the Jackson generator
     * (never the full payload in memory).
     *
     * @param objectCode object code stamped in the payload
     * @param columns    ordered column definitions
     * @param source     batched row source
     * @param batchSize  rows per fetch round trip
     * @param out        target stream (not closed by the implementation)
     * @return data rows written
     */
    long writeJson(String objectCode, List<ExportColumn> columns, SheetRowSource source, int batchSize,
            OutputStream out);

    /**
     * Stream the sheet as a single-sheet xlsx workbook (POI SXSSF sliding
     * window — rows beyond the window are flushed to a temp file).
     *
     * @param sheetName sheet name
     * @param columns   ordered column definitions
     * @param source    batched row source
     * @param batchSize rows per fetch round trip
     * @param out       target stream (not closed by the implementation)
     * @return data rows written
     */
    long writeXlsx(String sheetName, List<ExportColumn> columns, SheetRowSource source, int batchSize,
            OutputStream out);
}
