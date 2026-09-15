package com.klsjnh.domain.lowcode011.enums;

/*                ServiceObjectType011 enum
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.11
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.11  service object type 011 class
 *      2026.09.15  migrated to domain lowcode011
 *
 */

import java.util.Locale;

/**
 * Low-code service object type value object: declares the binding scope of a
 * metadata service definition.
 * <p>
 * A service either executes in a global scope or against a single table row.
 * Immutable and compared by value.
 * </p>
 */

public enum ServiceObjectType011 {

    /** Service executes in a global scope. */
    GLOBAL_METHOD("global_method", "global method"),

    /** Service executes against a single table row. */
    TABLE_ROW_METHOD("table_row_method", "table row method");

    /**
     * Persistence code, string.
     */
    private final String code;

    /**
     * Display label, string.
     */
    private final String label;

    /**
     * Create a service object type constant.
     *
     * @param code  persistence code
     * @param label display label
     */
    ServiceObjectType011(String code, String label) {
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
    public static ServiceObjectType011 fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String v = value.trim().toLowerCase(Locale.ROOT);

        for (ServiceObjectType011 type : values()) {
            if (type.code.equals(v)) {
                return type;
            }
        }

        return null;
    }
}
