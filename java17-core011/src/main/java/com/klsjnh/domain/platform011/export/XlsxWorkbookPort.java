package com.klsjnh.domain.platform011.export;

/*                XlsxWorkbookPort interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.22  xlsx workbook port interface
 *
 */

import com.klsjnh.domain.platform011.importdata.ImportBundle;

import java.util.List;

/**
 * Port for encoding / decoding xlsx workbooks. Implemented in infrastructure
 * with Apache POI; application never depends on POI types.
 */

public interface XlsxWorkbookPort {

    /**
     * Encode a multi-sheet workbook to xlsx bytes.
     *
     * @param workbook export workbook
     * @return xlsx bytes
     */
    byte[] write(ExportWorkbook workbook);

    /**
     * Decode xlsx bytes into an import bundle using the given sheet specs
     * (header row = column codes).
     *
     * @param objectCode object code stamped on the bundle
     * @param bytes      xlsx bytes
     * @param sheetSpecs expected sheets and columns
     * @return import bundle
     */
    ImportBundle read(String objectCode, byte[] bytes, List<ExportSheetSpec> sheetSpecs);
}
