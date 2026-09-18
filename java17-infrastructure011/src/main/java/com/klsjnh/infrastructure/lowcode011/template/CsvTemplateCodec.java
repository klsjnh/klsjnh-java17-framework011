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

import com.klsjnh.domain.lowcode011.TemplateCodec;
import com.klsjnh.domain.lowcode011.enums.TemplateFormat011;
import com.klsjnh.domain.lowcode011.records.MetaDtoKey011;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * CSV template codec: a sectioned single file. Each section starts with
 * {@code ##<name>} ({@code metaData} / {@code fieldData} / {@code displayData} /
 * {@code serviceData} / {@code source}) and holds CSV rows; {@code metaData} and
 * {@code source} are {@code key,value} pairs, the children use a fixed header.
 * Export is UTF-8 with BOM (Excel friendly); import tolerates BOM and maps
 * typed columns back to the MetaDTO shape.
 */

@Component
public class CsvTemplateCodec implements TemplateCodec {

    /**
     * UTF-8 byte order mark.
     */
    private static final String BOM = "\uFEFF";

    /**
     * fieldData columns.
     */
    private static final String[] FIELD_COLUMNS = { "code", "name", "fieldType", "length", "notNull", "defaultValue",
            "sort" };

    /**
     * displayData columns.
     */
    private static final String[] DISPLAY_COLUMNS = { "code", "name", "align", "width", "componentType", "displayType",
            "param011", "sort" };

    /**
     * serviceData columns.
     */
    private static final String[] SERVICE_COLUMNS = { "code", "name", "description", "objectType", "paramType",
            "serviceContent", "enabled", "sort" };

    /**
     * Canonical child column keys by lower-case name.
     */
    private static final Map<String, String> COLUMN_KEYS = Map.ofEntries(
            Map.entry("code", "code"), Map.entry("name", "name"), Map.entry("fieldtype", "fieldType"),
            Map.entry("length", "length"), Map.entry("notnull", "notNull"),
            Map.entry("defaultvalue", "defaultValue"), Map.entry("sort", "sort"), Map.entry("align", "align"),
            Map.entry("width", "width"), Map.entry("componenttype", "componentType"),
            Map.entry("displaytype", "displayType"), Map.entry("param011", "param011"),
            Map.entry("description", "description"), Map.entry("objecttype", "objectType"),
            Map.entry("paramtype", "paramType"), Map.entry("servicecontent", "serviceContent"),
            Map.entry("enabled", "enabled"));

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

        sb.append("##").append(MetaDtoKey011.META_DATA).append('\n');

        if (metaData != null) {
            for (Map.Entry<String, Object> entry : metaData.entrySet()) {
                if (MetaDtoKey011.SOURCE.equals(entry.getKey()) || entry.getValue() instanceof Map
                        || entry.getValue() instanceof List) {
                    continue;
                }

                sb.append(csvRow(entry.getKey(), str(entry.getValue()))).append('\n');
            }
        }

        appendSection(sb, MetaDtoKey011.FIELD_DATA, FIELD_COLUMNS, asList(template.get(MetaDtoKey011.FIELD_DATA)));
        appendSection(sb, MetaDtoKey011.DISPLAY_DATA, DISPLAY_COLUMNS, asList(template.get(MetaDtoKey011.DISPLAY_DATA)));
        appendSection(sb, MetaDtoKey011.SERVICE_DATA, SERVICE_COLUMNS, asList(template.get(MetaDtoKey011.SERVICE_DATA)));

        Map<String, Object> source = metaData == null ? null : asMap(metaData.get(MetaDtoKey011.SOURCE));

        if (source != null && !source.isEmpty()) {
            sb.append("##").append(MetaDtoKey011.SOURCE).append('\n');
            for (Map.Entry<String, Object> entry : source.entrySet()) {
                sb.append(csvRow(entry.getKey(), str(entry.getValue()))).append('\n');
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

            if (line.startsWith("##")) {
                section = line.substring(2).trim().toLowerCase(Locale.ROOT);
                header = null;
                continue;
            }

            List<String> cells = parseRow(line);

            if (section == null) {
                continue;
            }

            switch (section) {
                case "metadata":
                    if (cells.size() >= 2 && !cells.get(0).isBlank()) {
                        putMeta(metaData, cells.get(0).trim(), cells.get(1));
                    }
                    break;
                case "source":
                    if (cells.size() >= 2 && !cells.get(0).isBlank()) {
                        source.put(cells.get(0).trim(), blankToNull(cells.get(1)));
                    }
                    break;
                case "fielddata":
                    if (header == null) {
                        header = canonicalHeader(cells);
                    } else {
                        addChild(fields, header, cells);
                    }
                    break;
                case "displaydata":
                    if (header == null) {
                        header = canonicalHeader(cells);
                    } else {
                        addChild(displays, header, cells);
                    }
                    break;
                case "servicedata":
                    if (header == null) {
                        header = canonicalHeader(cells);
                    } else {
                        addChild(services, header, cells);
                    }
                    break;
                default:
                    break;
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
     * Append a section (header + rows).
     *
     * @param sb      target
     * @param name    section name
     * @param columns column keys
     * @param rows    rows
     */
    private void appendSection(StringBuilder sb, String name, String[] columns, List<Map<String, Object>> rows) {
        sb.append("##").append(name).append('\n');
        sb.append(csvRow(columns)).append('\n');

        for (Map<String, Object> row : rows) {
            String[] cells = new String[columns.length];

            for (int i = 0; i < columns.length; i++) {
                cells[i] = str(row.get(columns[i]));
            }

            sb.append(csvRow(cells)).append('\n');
        }
    }

    /**
     * Add one child row using the header mapping and typed coercion.
     *
     * @param target target list
     * @param header lower-case header cells
     * @param cells  row cells
     */
    private void addChild(List<Map<String, Object>> target, List<String> header, List<String> cells) {
        Map<String, Object> row = new LinkedHashMap<>();

        for (int i = 0; i < header.size() && i < cells.size(); i++) {
            row.put(header.get(i), coerce(header.get(i), cells.get(i)));
        }

        target.add(row);
    }

    /**
     * Coerce a CSV cell to the JSON value type expected by the validator.
     *
     * @param key   column key (lower case)
     * @param value raw cell
     * @return typed value
     */
    private Object coerce(String key, String value) {
        String v = value == null ? "" : value.trim();

        switch (key == null ? "" : key.toLowerCase(Locale.ROOT)) {
            case "length":
            case "width":
            case "sort":
                return v.isEmpty() ? null : Integer.valueOf(v);
            case "notnull":
            case "enabled":
                return "true".equalsIgnoreCase(v) || "1".equals(v) || "yes".equalsIgnoreCase(v)
                        || "y".equalsIgnoreCase(v);
            default:
                return blankToNull(v);
        }
    }

    /**
     * Put a metaData pair, canonicalising known keys and types.
     *
     * @param metaData target
     * @param key      raw key
     * @param value    raw value
     */
    private void putMeta(Map<String, Object> metaData, String key, String value) {
        String lower = key.toLowerCase(Locale.ROOT);

        if (MetaDtoKey011.SORT_ORDER.toLowerCase(Locale.ROOT).equals(lower)) {
            String v = value == null ? "" : value.trim();
            metaData.put(MetaDtoKey011.SORT_ORDER, v.isEmpty() ? null : Integer.valueOf(v));
            return;
        }

        for (String canonical : new String[] { MetaDtoKey011.OBJECT_NAME, MetaDtoKey011.OBJECT_TYPE,
                MetaDtoKey011.DESCRIPTION, MetaDtoKey011.BUSINESS_FIELD, MetaDtoKey011.PACKAGE_NAME,
                MetaDtoKey011.ROUTER_PATH, MetaDtoKey011.REMARK }) {
            if (canonical.toLowerCase(Locale.ROOT).equals(lower)) {
                metaData.put(canonical, blankToNull(value));
                return;
            }
        }
    }

    /**
     * Whether a value is blank.
     *
     * @param value value
     * @return value or null when blank
     */
    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    /**
     * Map header cells to canonical child column keys (case-insensitive).
     *
     * @param cells header cells
     * @return canonical keys
     */
    private List<String> canonicalHeader(List<String> cells) {
        List<String> out = new ArrayList<>();

        for (String cell : cells) {
            String lower = cell == null ? "" : cell.trim().toLowerCase(Locale.ROOT);
            out.add(COLUMN_KEYS.getOrDefault(lower, lower));
        }

        return out;
    }

    /**
     * String form of a value (null becomes empty).
     *
     * @param value value
     * @return string
     */
    private String str(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    /**
     * Build one CSV row with escaping.
     *
     * @param cells cells
     * @return row text
     */
    private String csvRow(String... cells) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < cells.length; i++) {
            if (i > 0) {
                sb.append(',');
            }

            sb.append(csvCell(cells[i]));
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
