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
 * Markdown template codec: a human-friendly document where each section is a
 * table ({@code metaData} / {@code fieldData} / {@code displayData} /
 * {@code serviceData} / {@code source}). Table syntax comes from
 * {@link MarkdownUtil011}, column/typing semantics from
 * {@link TemplateChildSupport}; this codec holds no magic keys.
 */

@Component
public class MarkdownTemplateCodec implements TemplateCodec {

    /**
     * Document title.
     */
    private static final String TITLE = "# Template";

    /**
     * Heading marker.
     */
    private static final String HEADING_PREFIX = "#";

    /**
     * Key/value table header.
     */
    private static final List<String> KEY_VALUE_HEADER = List.of("key", "value");

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

        sb.append(TITLE).append("\n\n");
        appendTable(sb, MetaDtoKey011.META_DATA, KEY_VALUE_HEADER, TemplateChildSupport.keyValues(metaData, true));
        appendChildTable(sb, MetaDtoKey011.FIELD_DATA, TemplateChildSupport.FIELD_COLUMNS,
                asList(template.get(MetaDtoKey011.FIELD_DATA)));
        appendChildTable(sb, MetaDtoKey011.DISPLAY_DATA, TemplateChildSupport.DISPLAY_COLUMNS,
                asList(template.get(MetaDtoKey011.DISPLAY_DATA)));
        appendChildTable(sb, MetaDtoKey011.SERVICE_DATA, TemplateChildSupport.SERVICE_COLUMNS,
                asList(template.get(MetaDtoKey011.SERVICE_DATA)));

        Map<String, Object> source = metaData == null ? null : asMap(metaData.get(MetaDtoKey011.SOURCE));

        if (source != null && !source.isEmpty()) {
            appendTable(sb, MetaDtoKey011.SOURCE, KEY_VALUE_HEADER, TemplateChildSupport.keyValues(source, false));
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

            if (trimmed.startsWith(HEADING_PREFIX)) {
                section = trimmed.replaceFirst("^#+", "").trim();
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
     * Append a section table from ready rows.
     *
     * @param sb      target
     * @param name    section name
     * @param header  header cells
     * @param rows    rows
     */
    private void appendTable(StringBuilder sb, String name, List<String> header, List<List<String>> rows) {
        sb.append("## ").append(name).append("\n\n");
        sb.append(MarkdownUtil011.renderTable(header, rows)).append('\n');
    }

    /**
     * Append a child section table.
     *
     * @param sb      target
     * @param name    section name
     * @param columns column keys
     * @param rows    rows
     */
    private void appendChildTable(StringBuilder sb, String name, List<String> columns,
            List<Map<String, Object>> rows) {
        List<List<String>> table = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            List<String> cells = new ArrayList<>();

            for (String column : columns) {
                cells.add(TemplateChildSupport.str(row.get(column)));
            }

            table.add(cells);
        }

        appendTable(sb, name, columns, table);
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
