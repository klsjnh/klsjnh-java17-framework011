package com.klsjnh.application.lowcode011;

/*                RowView record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  dynamic row view record
 *
 */

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Map;

/**
 * Typed view of one dynamic row: the raw column map is contained here instead of
 * leaking a bare {@code Map} through the layers. Serialized flat (via
 * {@link JsonValue}) so the wire shape stays the same as before.
 *
 * @param values column name → value
 */

public record RowView(Map<String, Object> values) {

    /**
     * Build a row view, treating null as empty.
     *
     * @param values column values
     * @return row view
     */
    public static RowView of(Map<String, Object> values) {
        return new RowView(values == null ? Map.of() : values);
    }

    /**
     * Flat json shape (the column map itself).
     *
     * @return column values
     */
    @JsonValue
    public Map<String, Object> json() {
        return values;
    }
}
