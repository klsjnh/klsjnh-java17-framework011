package com.klsjnh.infrastructure.platform011.export;

/*                ExportStreamCodec011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.06
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.06  streaming export codec (SXSSF xlsx / RFC 4180 csv / json
 *                  generator) — memory bounded at one batch
 *
 */

import com.klsjnh.domain.platform011.export.ExportColumn;
import com.klsjnh.domain.platform011.export.ExportStreamPort;
import com.klsjnh.domain.platform011.export.SheetRowSource;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Streaming export codec implementation. Every writer walks the
 * {@link SheetRowSource} batch by batch ({@code batchSize} rows per fetch) and
 * writes straight to the target stream: CSV row-by-row through a
 * {@link BufferedWriter}, JSON through the Jackson {@link JsonGenerator}, xlsx
 * through a POI {@link SXSSFWorkbook sliding window} (rows beyond the window
 * are flushed to a temp file). Memory never holds more than one batch.
 */

@Component
public class ExportStreamCodec011 implements ExportStreamPort {

    /**
     * JSON factory (thread-safe, reused).
     */
    private static final JsonFactory JSON_FACTORY = new JsonFactory();

    /**
     * CSV cell / value escaper: RFC 4180 — a value containing comma, quote, CR
     * or LF is wrapped in quotes and inner quotes are doubled.
     *
     * @param value raw cell value, nullable
     * @return encoded cell
     */
    static String csvCell(Object value) {
        if (value == null) {
            return "";
        }

        String text = String.valueOf(value);

        if (text.indexOf(',') >= 0 || text.indexOf('"') >= 0 || text.indexOf('\r') >= 0 || text.indexOf('\n') >= 0) {
            return '"' + text.replace("\"", "\"\"") + '"';
        }

        return text;
    }

    /**
     * Stream as RFC 4180 CSV: UTF-8 BOM (Excel), header = column names, CRLF
     * line separator.
     */
    @Override
    public long writeCsv(String sheetName, List<ExportColumn> columns, SheetRowSource source, int batchSize,
            OutputStream out) {
        long rows = 0;

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
            writer.write('\uFEFF');
            writer.write(joinCsv(names(columns)));
            writer.write("\r\n");

            int offset = 0;
            List<Map<String, Object>> batch;

            while (!(batch = source.fetch(offset, batchSize)).isEmpty()) {
                for (Map<String, Object> row : batch) {
                    writer.write(joinCsv(values(columns, row)));
                    writer.write("\r\n");
                    rows++;
                }
                offset += batch.size();
            }

            writer.flush();
        } catch (IOException ex) {
            throw new IllegalStateException("csv export streaming failed", ex);
        }

        return rows;
    }

    /**
     * Stream as JSON via the Jackson generator.
     */
    @Override
    public long writeJson(String objectCode, List<ExportColumn> columns, SheetRowSource source, int batchSize,
            OutputStream out) {
        long rows = 0;

        try (JsonGenerator json = JSON_FACTORY.createGenerator(out, JsonEncoding.UTF8)) {
            json.writeStartObject();
            json.writeStringField("objectCode", objectCode);
            json.writeFieldName("columns");
            json.writeStartArray();

            for (ExportColumn column : columns) {
                json.writeStartObject();
                json.writeStringField("code", column.code());
                json.writeStringField("name", column.name());
                json.writeEndObject();
            }

            json.writeEndArray();
            json.writeFieldName("rows");
            json.writeStartArray();

            int offset = 0;
            List<Map<String, Object>> batch;

            while (!(batch = source.fetch(offset, batchSize)).isEmpty()) {
                for (Map<String, Object> row : batch) {
                    json.writeStartObject();

                    for (ExportColumn column : columns) {
                        writeValue(json, column.code(), row.get(column.code()));
                    }

                    json.writeEndObject();
                    rows++;
                }
                offset += batch.size();
            }

            json.writeEndArray();
            json.writeNumberField("rowCount", rows);
            json.writeEndObject();
            json.flush();
        } catch (IOException ex) {
            throw new IllegalStateException("json export streaming failed", ex);
        }

        return rows;
    }

    /**
     * Stream as single-sheet xlsx through a POI SXSSF sliding window.
     */
    @Override
    public long writeXlsx(String sheetName, List<ExportColumn> columns, SheetRowSource source, int batchSize,
            OutputStream out) {
        long rows = 0;

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
            Sheet sheet = workbook.createSheet(sheetName);
            Row header = sheet.createRow(0);

            for (int i = 0; i < columns.size(); i++) {
                header.createCell(i).setCellValue(columns.get(i).name());
            }

            int offset = 0;
            int rowIndex = 1;
            List<Map<String, Object>> batch;

            while (!(batch = source.fetch(offset, batchSize)).isEmpty()) {
                for (Map<String, Object> row : batch) {
                    Row sheetRow = sheet.createRow(rowIndex++);
                    for (int i = 0; i < columns.size(); i++) {
                        Object value = row.get(columns.get(i).code());
                        sheetRow.createCell(i).setCellValue(value == null ? "" : String.valueOf(value));
                    }
                    rows++;
                }
                offset += batch.size();
            }

            workbook.write(out);
            workbook.dispose();
        } catch (IOException ex) {
            throw new IllegalStateException("xlsx export streaming failed", ex);
        }

        return rows;
    }

    /**
     * Write one cell value with its natural JSON type (string / number /
     * boolean / null).
     *
     * @param json  generator
     * @param code  field name
     * @param value raw value, nullable
     * @throws IOException generator failure
     */
    private void writeValue(JsonGenerator json, String code, Object value) throws IOException {
        if (value == null) {
            json.writeNullField(code);
            return;
        }

        if (value instanceof Integer || value instanceof Long) {
            json.writeNumberField(code, ((Number) value).longValue());
            return;
        }

        if (value instanceof Number number) {
            json.writeNumberField(code, number.doubleValue());
            return;
        }

        if (value instanceof Boolean flag) {
            json.writeBooleanField(code, flag);
            return;
        }

        json.writeStringField(code, String.valueOf(value));
    }

    /**
     * Join encoded cells with commas.
     *
     * @param cells encoded cell values
     * @return one CSV line
     */
    private String joinCsv(List<String> cells) {
        StringBuilder line = new StringBuilder();

        for (int i = 0; i < cells.size(); i++) {
            if (i > 0) {
                line.append(',');
            }
            line.append(cells.get(i));
        }

        return line.toString();
    }

    /**
     * Column display names.
     *
     * @param columns column definitions
     * @return names
     */
    private List<String> names(List<ExportColumn> columns) {
        return columns.stream().map(ExportColumn::name).toList();
    }

    /**
     * Row values in column order, CSV-encoded.
     *
     * @param columns column definitions
     * @param row     raw row
     * @return encoded values
     */
    private List<String> values(List<ExportColumn> columns, Map<String, Object> row) {
        return columns.stream().map(column -> csvCell(row.get(column.code()))).toList();
    }
}
