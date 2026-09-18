package com.klsjnh.infrastructure.lowcode011.template;

/*                TemplateChildSupport class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.18
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.18  template child column/normalise support class
 *
 */

import com.klsjnh.domain.lowcode011.records.MetaDtoKey011;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Shared helpers for the table-shaped template codecs: the child column layout,
 * canonical key resolution, typed coercion and section row building. All keys
 * come from {@link MetaDtoKey011} so the codecs hold no magic strings.
 */

final class TemplateChildSupport {

    /**
     * Known MetaDTO keys, for case-insensitive canonicalisation.
     */
    private static final List<String> KNOWN_KEYS = List.of(MetaDtoKey011.OBJECT_NAME, MetaDtoKey011.OBJECT_TYPE,
            MetaDtoKey011.DESCRIPTION, MetaDtoKey011.BUSINESS_FIELD, MetaDtoKey011.PACKAGE_NAME,
            MetaDtoKey011.ROUTER_PATH, MetaDtoKey011.REMARK, MetaDtoKey011.SORT_ORDER, MetaDtoKey011.CODE,
            MetaDtoKey011.NAME, MetaDtoKey011.FIELD_TYPE, MetaDtoKey011.LENGTH, MetaDtoKey011.NOT_NULL,
            MetaDtoKey011.DEFAULT_VALUE, MetaDtoKey011.SORT, MetaDtoKey011.ALIGN, MetaDtoKey011.WIDTH,
            MetaDtoKey011.COMPONENT_TYPE, MetaDtoKey011.DISPLAY_TYPE, MetaDtoKey011.PARAM011, MetaDtoKey011.PARAM_TYPE,
            MetaDtoKey011.SERVICE_CONTENT, MetaDtoKey011.ENABLED);

    /**
     * fieldData columns.
     */
    static final List<String> FIELD_COLUMNS = List.of(MetaDtoKey011.CODE, MetaDtoKey011.NAME, MetaDtoKey011.FIELD_TYPE,
            MetaDtoKey011.LENGTH, MetaDtoKey011.NOT_NULL, MetaDtoKey011.DEFAULT_VALUE, MetaDtoKey011.SORT);

    /**
     * displayData columns.
     */
    static final List<String> DISPLAY_COLUMNS = List.of(MetaDtoKey011.CODE, MetaDtoKey011.NAME, MetaDtoKey011.ALIGN,
            MetaDtoKey011.WIDTH, MetaDtoKey011.COMPONENT_TYPE, MetaDtoKey011.DISPLAY_TYPE, MetaDtoKey011.PARAM011,
            MetaDtoKey011.SORT);

    /**
     * serviceData columns.
     */
    static final List<String> SERVICE_COLUMNS = List.of(MetaDtoKey011.CODE, MetaDtoKey011.NAME,
            MetaDtoKey011.DESCRIPTION, MetaDtoKey011.OBJECT_TYPE, MetaDtoKey011.PARAM_TYPE,
            MetaDtoKey011.SERVICE_CONTENT, MetaDtoKey011.ENABLED, MetaDtoKey011.SORT);

    /**
     * Utility: no instances.
     */
    private TemplateChildSupport() {
    }

    /**
     * Resolve a raw header key to the canonical MetaDTO key (case-insensitive);
     * unknown keys are returned lower-cased.
     *
     * @param raw raw key
     * @return canonical key
     */
    static String canonical(String raw) {
        String value = raw == null ? "" : raw.trim();

        for (String key : KNOWN_KEYS) {
            if (key.equalsIgnoreCase(value)) {
                return key;
            }
        }

        return value.toLowerCase(Locale.ROOT);
    }

    /**
     * Map header cells to canonical keys.
     *
     * @param cells header cells
     * @return canonical keys
     */
    static List<String> canonicalHeader(List<String> cells) {
        List<String> out = new ArrayList<>();

        for (String cell : cells) {
            out.add(canonical(cell));
        }

        return out;
    }

    /**
     * Coerce a string cell to the JSON value type the validator expects.
     *
     * @param key   canonical key
     * @param value raw cell
     * @return typed value
     */
    static Object coerce(String key, String value) {
        String v = value == null ? "" : value.trim();

        if (MetaDtoKey011.LENGTH.equals(key) || MetaDtoKey011.WIDTH.equals(key) || MetaDtoKey011.SORT.equals(key)) {
            return v.isEmpty() ? null : Integer.valueOf(v);
        }

        if (MetaDtoKey011.NOT_NULL.equals(key) || MetaDtoKey011.ENABLED.equals(key)) {
            return "true".equalsIgnoreCase(v) || "1".equals(v) || "yes".equalsIgnoreCase(v) || "y".equalsIgnoreCase(v);
        }

        return blankToNull(v);
    }

    /**
     * Add one child row using the header mapping and typed coercion.
     *
     * @param target target list
     * @param header canonical header cells
     * @param cells  row cells
     */
    static void addChild(List<Map<String, Object>> target, List<String> header, List<String> cells) {
        Map<String, Object> row = new LinkedHashMap<>();

        for (int i = 0; i < header.size() && i < cells.size(); i++) {
            row.put(header.get(i), coerce(header.get(i), cells.get(i)));
        }

        target.add(row);
    }

    /**
     * Put a metaData pair, canonicalising the key and typing the value.
     *
     * @param metaData target
     * @param key      raw key
     * @param value    raw value
     */
    static void putMeta(Map<String, Object> metaData, String key, String value) {
        String canonical = canonical(key);

        if (MetaDtoKey011.SORT_ORDER.equals(canonical)) {
            String v = value == null ? "" : value.trim();
            metaData.put(canonical, v.isEmpty() ? null : Integer.valueOf(v));
        } else {
            metaData.put(canonical, blankToNull(value));
        }
    }

    /**
     * Build the key/value rows of a scalar map.
     *
     * @param map           source map, nullable
     * @param skipSourceKey whether to skip the nested {@code source} entry
     * @return rows
     */
    static List<List<String>> keyValues(Map<String, Object> map, boolean skipSourceKey) {
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
     * Whether a value is blank.
     *
     * @param value value
     * @return value or null when blank
     */
    static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    /**
     * String form of a value (null becomes empty).
     *
     * @param value value
     * @return string
     */
    static String str(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
