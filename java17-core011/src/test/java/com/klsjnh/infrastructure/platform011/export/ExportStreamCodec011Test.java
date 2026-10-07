package com.klsjnh.infrastructure.platform011.export;

/*                ExportStreamCodec011Test class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.06
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.06  streaming codec unit test (csv escape / batch loop / json)
 *
 */

import com.klsjnh.domain.platform011.export.ExportColumn;
import com.klsjnh.domain.platform011.export.SheetRowSource;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for {@link ExportStreamCodec011}: RFC 4180 escaping, the batch
 * loop (multi-batch walk with a small batch size) and the JSON payload shape.
 */

class ExportStreamCodec011Test {

    /**
     * Codec under test.
     */
    private final ExportStreamCodec011 codec = new ExportStreamCodec011();

    /**
     * Sample columns.
     */
    private final List<ExportColumn> columns = List.of(new ExportColumn("id", "ID"), new ExportColumn("name", "名称"));

    /**
     * CSV escapes comma / quote / newline cells and writes BOM + header.
     */
    @Test
    void csvEscapesAndWritesBom() {
        SheetRowSource source = (offset, limit) -> offset == 0
                ? List.of(Map.of("id", 1, "name", "a,b"), Map.of("id", 2, "name", "say \"hi\"\nnext"))
                : List.of();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        long rows = codec.writeCsv("master", columns, source, 500, out);

        String text = out.toString(StandardCharsets.UTF_8);

        assertEquals(2, rows);
        assertTrue(text.startsWith("\uFEFF"));
        assertTrue(text.contains("ID,名称\r\n"));
        assertTrue(text.contains("1,\"a,b\"\r\n"));
        assertTrue(text.contains("2,\"say \"\"hi\"\"\nnext\"\r\n"));
    }

    /**
     * The batch loop walks multiple fetches until a short batch.
     */
    @Test
    void csvWalksBatches() {
        List<Map<String, Object>> all = List.of(Map.of("id", 1, "name", "a"), Map.of("id", 2, "name", "b"),
                Map.of("id", 3, "name", "c"));
        SheetRowSource source = (offset, limit) -> all.stream().skip(offset).limit(limit).toList();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        long rows = codec.writeCsv("master", columns, source, 2, out);

        assertEquals(3, rows);
        assertEquals(4, out.toString(StandardCharsets.UTF_8).lines().count());
    }

    /**
     * JSON payload shape: objectCode + columns + rows + trailing rowCount.
     */
    @Test
    void jsonShape() {
        SheetRowSource source = (offset, limit) -> offset == 0
                ? List.of(Map.of("id", 1, "name", "n1"))
                : List.of();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        long rows = codec.writeJson("julyUser", columns, source, 500, out);

        String text = out.toString(StandardCharsets.UTF_8);

        assertEquals(1, rows);
        assertTrue(text.startsWith("{\"objectCode\":\"julyUser\""));
        assertTrue(text.contains("\"columns\":[{\"code\":\"id\",\"name\":\"ID\"}"));
        assertTrue(text.contains("\"rows\":[{\"id\":1,\"name\":\"n1\"}]"));
        assertTrue(text.contains("\"rowCount\":1}"));
    }

    /**
     * Xlsx output is a zip container (PK magic) with the streamed row count.
     */
    @Test
    void xlsxStreamsZipContainer() {
        SheetRowSource source = (offset, limit) -> offset == 0
                ? List.of(Map.of("id", 1, "name", "a"), Map.of("id", 2, "name", "b"))
                : List.of();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        long rows = codec.writeXlsx("master", columns, source, 500, out);

        assertEquals(2, rows);
        byte[] bytes = out.toByteArray();
        assertEquals('P', bytes[0]);
        assertEquals('K', bytes[1]);
    }
}
