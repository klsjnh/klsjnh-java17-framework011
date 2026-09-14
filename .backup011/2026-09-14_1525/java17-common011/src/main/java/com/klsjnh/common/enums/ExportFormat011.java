package com.klsjnh.common.enums;

/*                ExportFormat011 enum
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  export format 011 class
 *
 */

/**
 * Data export format for the platform export feature.
 */

public enum ExportFormat011 {

    /** Pretty-printed JSON (application/json). */
    JSON("json"),

    /** CSV with UTF-8 BOM for Excel (text/csv). */
    CSV("csv");

    /**
     * Config code.
     */
    private final String code;

    /**
     * Create a format constant.
     *
     * @param code config code
     */
    ExportFormat011(String code) {
        this.code = code;
    }

    /**
     * Get the config code.
     *
     * @return config code (json / csv)
     */
    public String getCode() {
        return code;
    }

    /**
     * Resolve from a raw value; null when blank or unknown.
     *
     * @param value raw format value
     * @return resolved constant, null when blank or unknown
     */
    public static ExportFormat011 of(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        for (ExportFormat011 format : values()) {
            if (format.code.equals(value)) {
                return format;
            }
        }

        return null;
    }
}
