package com.klsjnh.domain.lowcode011.enums;

/*                ServiceParamType011 enum
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.11
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.11  service param type 011 class
 *      2026.09.15  migrated to domain lowcode011
 *
 */

import java.util.Locale;

/**
 * Low-code service parameter type value object: declares the payload shape a
 * metadata service expects.
 * <p>
 * Drives both the generated invocation signature and the request binding rule.
 * Immutable and compared by value.
 * </p>
 */

public enum ServiceParamType011 {

    /** No parameter. */
    NONE("none", "no param"),

    /** Single string parameter. */
    STRING("string", "string param"),

    /** Structured object parameter. */
    OBJECT("object", "object param"),

    /** Single uploaded file. */
    SINGLE_FILE("single_file", "single file upload"),

    /** Multiple uploaded files. */
    MULTI_FILE("multi_file", "multiple files upload");

    /**
     * Persistence code, string.
     */
    private final String code;

    /**
     * Display label, string.
     */
    private final String label;

    /**
     * Create a service param type constant.
     *
     * @param code  persistence code
     * @param label display label
     */
    ServiceParamType011(String code, String label) {
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
    public static ServiceParamType011 fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String v = value.trim().toLowerCase(Locale.ROOT);

        for (ServiceParamType011 type : values()) {
            if (type.code.equals(v)) {
                return type;
            }
        }

        return null;
    }
}
