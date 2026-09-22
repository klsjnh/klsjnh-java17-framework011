package com.klsjnh.infrastructure.platform011.export;

/*                XlsxWorkbookCodec011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.22  xlsx workbook codec 011 class
 *
 */

import com.klsjnh.common.exception.BusinessException;

import com.klsjnh.domain.platform011.export.ExportColumn;
import com.klsjnh.domain.platform011.export.ExportSheet;
import com.klsjnh.domain.platform011.export.ExportSheetSpec;
import com.klsjnh.domain.platform011.export.ExportWorkbook;
import com.klsjnh.domain.platform011.export.XlsxWorkbookPort;
import com.klsjnh.domain.platform011.importdata.ImportBundle;
import com.klsjnh.domain.platform011.importdata.ImportSheet;

import org.springframework.stereotype.Component;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Apache POI implementation of {@link XlsxWorkbookPort}: header row is column
 * codes; data rows follow.
 */

@Component
public class XlsxWorkbookCodec011 implements XlsxWorkbookPort {

    /**
     * Formats cell values as display strings.
     */
    private final DataFormatter formatter = new DataFormatter();

    /**
     * Encode a workbook to xlsx bytes.
     *
     * @param workbook export workbook
     * @return xlsx bytes
     */
    @Override
    public byte[] write(ExportWorkbook workbook) {
        String funcName = "xlsx write";

        try (Workbook xssf = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            for (ExportSheet sheet : workbook.sheets()) {
                Sheet xssfSheet = xssf.createSheet(sheet.name());
                Row header = xssfSheet.createRow(0);
                List<ExportColumn> columns = sheet.columns();

                for (int c = 0; c < columns.size(); c++) {
                    header.createCell(c).setCellValue(columns.get(c).code());
                }

                List<Map<String, Object>> rows = sheet.rows();

                for (int r = 0; r < rows.size(); r++) {
                    Row dataRow = xssfSheet.createRow(r + 1);
                    Map<String, Object> map = rows.get(r);

                    for (int c = 0; c < columns.size(); c++) {
                        Object value = map.get(columns.get(c).code());
                        Cell cell = dataRow.createCell(c);

                        if (value == null) {
                            cell.setBlank();
                        } else if (value instanceof Number number) {
                            cell.setCellValue(number.doubleValue());
                        } else {
                            cell.setCellValue(String.valueOf(value));
                        }
                    }
                }
            }

            xssf.write(out);

            return out.toByteArray();
        } catch (Exception ex) {
            throw BusinessException.badRequest(funcName + ": " + ex.getMessage());
        }
    }

    /**
     * Decode xlsx bytes using the expected sheet specs.
     *
     * @param objectCode object code
     * @param bytes      xlsx bytes
     * @param sheetSpecs expected sheets
     * @return import bundle
     */
    @Override
    public ImportBundle read(String objectCode, byte[] bytes, List<ExportSheetSpec> sheetSpecs) {
        String funcName = "xlsx read";

        if (bytes == null || bytes.length == 0) {
            throw BusinessException.badRequest(funcName + ": empty file");
        }

        try (Workbook xssf = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            List<ImportSheet> sheets = new ArrayList<>();

            for (ExportSheetSpec spec : sheetSpecs) {
                Sheet sheet = xssf.getSheet(spec.name());

                if (sheet == null) {
                    throw BusinessException.badRequest(funcName + ": missing sheet " + spec.name());
                }

                Row headerRow = sheet.getRow(0);

                if (headerRow == null) {
                    throw BusinessException.badRequest(funcName + ": empty header on sheet " + spec.name());
                }

                List<String> headers = new ArrayList<>();

                for (int c = 0; c < headerRow.getLastCellNum(); c++) {
                    headers.add(cellString(headerRow.getCell(c)).trim());
                }

                for (ExportColumn column : spec.columns()) {
                    if (!headers.contains(column.code())) {
                        throw BusinessException.badRequest(
                                funcName + ": sheet " + spec.name() + " missing column " + column.code());
                    }
                }

                List<Map<String, Object>> rows = new ArrayList<>();
                int last = sheet.getLastRowNum();

                for (int r = 1; r <= last; r++) {
                    Row dataRow = sheet.getRow(r);

                    if (dataRow == null || isBlankRow(dataRow, headers.size())) {
                        continue;
                    }

                    Map<String, Object> map = new LinkedHashMap<>();

                    for (ExportColumn column : spec.columns()) {
                        int index = headers.indexOf(column.code());
                        map.put(column.code(), cellString(dataRow.getCell(index)));
                    }

                    rows.add(map);
                }

                sheets.add(new ImportSheet(spec.name(), rows));
            }

            return new ImportBundle(objectCode, sheets);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.badRequest(funcName + ": " + ex.getMessage());
        }
    }

    /**
     * Whether every cell in the row is blank.
     *
     * @param row        row
     * @param columnCount columns to inspect
     * @return true when blank
     */
    private boolean isBlankRow(Row row, int columnCount) {
        for (int c = 0; c < columnCount; c++) {
            if (!cellString(row.getCell(c)).isBlank()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Read a cell as trimmed string.
     *
     * @param cell cell, nullable
     * @return string, never null
     */
    private String cellString(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return "";
        }

        return formatter.formatCellValue(cell).trim();
    }
}
