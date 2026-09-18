package com.klsjnh.infrastructure.lowcode011.template;

/*                MarkdownTemplateCodec class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.18
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.18  markdown template codec class (sectioned tables)
 *
 */

import com.klsjnh.common.util.MarkdownUtil011;

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
 * Markdown template codec: a human-friendly document where each section is a
 * table ({@code metaData} / {@code fieldData} / {@code displayData} /
 * {@code serviceData} / {@code source}). Table syntax comes from
 * {@link MarkdownUtil011}; this codec owns only the section/column/typing
 * semantics. Import reads the tables back into the MetaDTO shape, so the review
 * / deploy downstream stays unique.
 */

@Component
public class MarkdownTemplateCodec implements TemplateCodec {

    /**
     * Section headers.
     */
    private static final List<String> KEY_VALUE_HEADER = List.of("key", "value");

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
        return TemplateFormat011.MARKDOWN;
    }

    /** {@inheritDoc} */
    @Override
    public byte[] exportTemplate(Map<String, Object> template) {
        StringBuilder sb = new StringBuilder();
        Map<String, Object> metaData = asMap(template.get(MetaDtoKey011.META_DATA));

        sb.append("# Template\n\n## ").append(MetaDtoKey011.META_DATA).append("\n\n");
        sb.append(MarkdownUtil011.renderTable(KEY_VALUE_HEADER, keyValues(metaData, true))).append('\n');

        appendTable(sb, MetaDtoKey011.FIELD_DATA, FIELD_COLUMNS, asList(template.get(MetaDtoKey011.FIELD_DATA)));
        appendTable(sb, MetaDtoKey011.DISPLAY_DATA, DISPLAY_COLUMNS, asList(template.get(MetaDtoKey011.DISPLAY_DATA)));
        appendTable(sb, MetaDtoKey011.SERVICE_DATA, SERVICE_COLUMNS, asList(template.get(MetaDtoKey011.SERVICE_DATA)));

        Map<String, Object> source = metaData == null ? null : asMap(metaData.get(MetaDtoKey011.SOURCE));

        if (source != null && !source.isEmpty()) {
            sb.append("## ").append(MetaDtoKey011.SOURCE).append("\n\n");
            sb.append(MarkdownUtil011.renderTable(KEY_VALUE_HEADER, keyValues(source, false))).append('\n');
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, Object> importTemplate(byte[] data) {
        String text = new String(data, StandardCharsets.UTF_8);
        Map<String, Object> metaData = new LinkedHashMap<>();
        Map<String, Object> source = new LinkedHashMap<>();
        List<Map<String, Object>> fields = new ArrayList<>();
        List<Map<String, Object>> displays = new ArrayList<>();
        List<Map<String, Object>> services = new ArrayList<>();
        String section = null;
        List<String> header = null;

        for (String rawLine : text.split("\n", -1)) {
            String line = rawLine.endsWith("\r") ? rawLine.substring(0, rawLine.length() - 1) : rawLine;
            String trimmed = line.trim();

            if (trimmed.startsWith("#")) {
                section = trimmed.replaceFirst("^#+", "").trim().toLowerCase(Locale.ROOT);
                header = null;
                continue;
            }

            if (!MarkdownUtil011.isTableRow(line) || section == null) {
                continue;
            }

            List<String> cells = MarkdownUtil011.parseRow(line);

            if (MarkdownUtil011.isSeparatorRow(cells)) {
                continue;
            }

            switch (section) {
                case "metadata":
                    if (cells.size() >= 2 && !cells.get(0).isBlank()) {
                        putMeta(metaData, cells.get(0), cells.get(1));
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
     * Build the key/value rows of a scalar map.
     *
     * @param map            source map, nullable
     * @param skipSourceKey  whether to skip the nested {@code source} entry
     * @return rows
     */
    private List<List<String>> keyValues(Map<String, Object> map, boolean skipSourceKey) {
        List<List<String>> rows = new ArrayList<>();

        if (map != null) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (skipSourceKey && MetaDtoKey011.SOURCE.equals(entry.getKey())) {
                    continue;
                }

                if (entry.getValue() instanceof Map || entry.getValue() instanceof List) {
                    continue;
                }

                rows.add(List.of(entry.getKey(), str(entry.getValue())));
            }
        }

        return rows;
    }

    /**
     * Append a section table.
     *
     * @param sb      target
     * @param name    section name
     * @param columns column keys
     * @param rows    rows
     */
    private void appendTable(StringBuilder sb, String name, String[] columns, List<Map<String, Object>> rows) {
        List<List<String>> table = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            List<String> cells = new ArrayList<>();

            for (String column : columns) {
                cells.add(str(row.get(column)));
            }

            table.add(cells);
        }

        sb.append("## ").append(name).append("\n\n");
        sb.append(MarkdownUtil011.renderTable(List.of(columns), table)).append('\n');
    }

    /**
     * Add one child row using the header mapping and typed coercion.
     *
     * @param target target list
     * @param header canonical header cells
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
     * Coerce a cell to the JSON value type expected by the validator.
     *
     * @param key   column key
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
        String lower = key.trim().toLowerCase(Locale.ROOT);

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
     * Whether a value is blank.
     *
     * @param value value
     * @return value or null when blank
     */
    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
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
