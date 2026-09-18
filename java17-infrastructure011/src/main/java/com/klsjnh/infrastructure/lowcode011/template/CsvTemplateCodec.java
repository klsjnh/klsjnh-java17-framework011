package com.klsjnh.infrastructure.lowcode011.template;

/*                CsvTemplateCodec class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.18
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.18  csv template codec class (sectioned single file)
 *
 */

import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.lowcode011.TemplateCodec;
import com.klsjnh.domain.lowcode011.enums.TemplateFormat011;
import com.klsjnh.domain.lowcode011.records.MetaDtoKey011;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CSV template codec: a sectioned single file. Each section starts with
 * {@code ##<name>} ({@code metaData} / {@code fieldData} / {@code displayData} /
 * {@code serviceData} / {@code source}) and holds CSV rows; {@code metaData} and
 * {@code source} are key/value pairs, the children use the shared column layout.
 * Export is UTF-8 with BOM (Excel friendly); import tolerates BOM.
 */

@Component
public class CsvTemplateCodec implements TemplateCodec {

    /**
     * UTF-8 byte order mark.
     */
    private static final String BOM = "\uFEFF";

    /**
     * Section marker.
     */
    private static final String SECTION_PREFIX = "##";

    /** {@inheritDoc} */
    @Override
    public TemplateFormat011 format() {
        return TemplateFormat011.CSV;
    }

    /** {@inheritDoc} */
    @Override
    public byte[] exportTemplate(Map<String, Object> template) {
        StringBuilder sb = new StringBuilder();
        Map<String, Object> metaData = asMap(template.get(MetaDtoKey011.META_DATA));

        sb.append(SECTION_PREFIX).append(MetaDtoKey011.META_DATA).append('\n');

        for (List<String> row : TemplateChildSupport.keyValues(metaData, true)) {
            sb.append(csvRow(row)).append('\n');
        }

        appendSection(sb, MetaDtoKey011.FIELD_DATA, TemplateChildSupport.FIELD_COLUMNS,
                asList(template.get(MetaDtoKey011.FIELD_DATA)));
        appendSection(sb, MetaDtoKey011.DISPLAY_DATA, TemplateChildSupport.DISPLAY_COLUMNS,
                asList(template.get(MetaDtoKey011.DISPLAY_DATA)));
        appendSection(sb, MetaDtoKey011.SERVICE_DATA, TemplateChildSupport.SERVICE_COLUMNS,
                asList(template.get(MetaDtoKey011.SERVICE_DATA)));

        Map<String, Object> source = metaData == null ? null : asMap(metaData.get(MetaDtoKey011.SOURCE));

        if (source != null && !source.isEmpty()) {
            sb.append(SECTION_PREFIX).append(MetaDtoKey011.SOURCE).append('\n');
            for (List<String> row : TemplateChildSupport.keyValues(source, false)) {
                sb.append(csvRow(row)).append('\n');
            }
        }

        return (BOM + sb).getBytes(StandardCharsets.UTF_8);
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, Object> importTemplate(byte[] data) {
        String text = new String(data, StandardCharsets.UTF_8);

        if (text.startsWith(BOM)) {
            text = text.substring(1);
        }

        Map<String, Object> metaData = new LinkedHashMap<>();
        Map<String, Object> source = new LinkedHashMap<>();
        List<Map<String, Object>> fields = new ArrayList<>();
        List<Map<String, Object>> displays = new ArrayList<>();
        List<Map<String, Object>> services = new ArrayList<>();
        String section = null;
        List<String> header = null;

        for (String rawLine : text.split("\n", -1)) {
            String line = rawLine.endsWith("\r") ? rawLine.substring(0, rawLine.length() - 1) : rawLine;

            if (line.isBlank()) {
                continue;
            }

            if (line.startsWith(SECTION_PREFIX)) {
                section = line.substring(SECTION_PREFIX.length()).trim();
                header = null;
                continue;
            }

            if (section == null) {
                continue;
            }

            List<String> cells = parseRow(line);

            if (MetaDtoKey011.META_DATA.equalsIgnoreCase(section)) {
                if (cells.size() >= 2 && !cells.get(0).isBlank()) {
                    TemplateChildSupport.putMeta(metaData, cells.get(0), cells.get(1));
                }
            } else if (MetaDtoKey011.SOURCE.equalsIgnoreCase(section)) {
                if (cells.size() >= 2 && !cells.get(0).isBlank()) {
                    source.put(cells.get(0).trim(), StringUtil011.blankToNull(cells.get(1)));
                }
            } else if (MetaDtoKey011.FIELD_DATA.equalsIgnoreCase(section)) {
                header = sectionRows(fields, header, cells);
            } else if (MetaDtoKey011.DISPLAY_DATA.equalsIgnoreCase(section)) {
                header = sectionRows(displays, header, cells);
            } else if (MetaDtoKey011.SERVICE_DATA.equalsIgnoreCase(section)) {
                header = sectionRows(services, header, cells);
            }
        }

        Map<String, Object> template = new LinkedHashMap<>();
        template.put(MetaDtoKey011.META_DATA, metaData);
        template.put(MetaDtoKey011.FIELD_DATA, fields);
        template.put(MetaDtoKey011.DISPLAY_DATA, displays);
        template.put(MetaDtoKey011.SERVICE_DATA, services);

        if (!source.isEmpty()) {
            metaData.put(MetaDtoKey011.SOURCE, source);
        }

        return template;
    }

    /**
     * Feed a child section row (first row is the header).
     *
     * @param target target list
     * @param header current header, nullable
     * @param cells  row cells
     * @return (possibly new) header
     */
    private List<String> sectionRows(List<Map<String, Object>> target, List<String> header, List<String> cells) {
        if (header == null) {
            return TemplateChildSupport.canonicalHeader(cells);
        }

        TemplateChildSupport.addChild(target, header, cells);

        return header;
    }

    /**
     * Append a section (header + rows).
     *
     * @param sb      target
     * @param name    section name
     * @param columns column keys
     * @param rows    rows
     */
    private void appendSection(StringBuilder sb, String name, List<String> columns, List<Map<String, Object>> rows) {
        sb.append(SECTION_PREFIX).append(name).append('\n');
        sb.append(csvRow(columns)).append('\n');

        for (Map<String, Object> row : rows) {
            List<String> cells = new ArrayList<>();

            for (String column : columns) {
                cells.add(TemplateChildSupport.str(row.get(column)));
            }

            sb.append(csvRow(cells)).append('\n');
        }
    }

    /**
     * Build one CSV row with escaping.
     *
     * @param cells cells
     * @return row text
     */
    private String csvRow(List<String> cells) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < cells.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }

            sb.append(csvCell(cells.get(i)));
        }

        return sb.toString();
    }

    /**
     * Escape one CSV cell (quote when it holds a delimiter/quote/newline).
     *
     * @param cell cell
     * @return escaped cell
     */
    private String csvCell(String cell) {
        String value = cell == null ? "" : cell;

        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }

    /**
     * Parse one CSV row (quote aware, no embedded newlines).
     *
     * @param line row text
     * @return cells
     */
    private List<String> parseRow(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean quoted = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (quoted) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        cur.append('"');
                        i++;
                    } else {
                        quoted = false;
                    }
                } else {
                    cur.append(c);
                }
            } else if (c == '"') {
                quoted = true;
            } else if (c == ',') {
                out.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }

        out.add(cur.toString());

        return out;
    }

    /**
     * Cast a value to a map.
     *
     * @param value value
     * @return map or null
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        return value instanceof Map ? (Map<String, Object>) value : null;
    }

    /**
     * Cast a value to a list of maps.
     *
     * @param value value
     * @return list, never null
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> asList(Object value) {
        return value instanceof List ? (List<Map<String, Object>>) value : List.of();
    }
}
