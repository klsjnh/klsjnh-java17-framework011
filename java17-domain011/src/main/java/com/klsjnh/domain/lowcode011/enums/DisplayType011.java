package com.klsjnh.domain.lowcode011.enums;

/*                DisplayType011 enum
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.11
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.11  display type 011 class
 *      2026.09.15  migrated to domain lowcode011
 *
 */

import java.util.Locale;

/**
 * Low-code display type value object: declares which rendering surface a
 * display column belongs to.
 * <p>
 * A column may target the whole view, the table only, the form only, or a
 * custom rendering region. Immutable and compared by value.
 * </p>
 */

public enum DisplayType011 {

    /** Column participates in every surface. */
    ALL("all", "all display"),

    /** Column appears in the table surface only. */
    TABLE("table", "table display"),

    /** Column appears in the form surface only. */
    FORM("form", "form display"),

    /** Column rendered by a custom implementation. */
    CUSTOM("custom", "custom display");

    /**
     * Persistence code, string.
     */
    private final String code;

    /**
     * Display label, string.
     */
    private final String label;

    /**
     * Create a display type constant.
     *
     * @param code  persistence code
     * @param label display label
     */
    DisplayType011(String code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * Get the persistence code.
     *
     * @return persistence code
     */
    public String getCode() {
        return code;
    }

    /**
     * Get the display label.
     *
     * @return display label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Resolve from a raw code; null when blank or unknown.
     *
     * @param value raw code
     * @return resolved type, null when blank or unknown
     */
    public static DisplayType011 fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String v = value.trim().toLowerCase(Locale.ROOT);

        for (DisplayType011 type : values()) {
            if (type.code.equals(v)) {
                return type;
            }
        }

        return null;
    }
}
