package com.klsjnh.domain.lowcode011.enums;

/*                TemplateFormat011 enum
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.18
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.18  template file format enum
 *
 */

import java.util.Locale;

/**
 * Template file formats: the stream representation of a model template. JSON is
 * the canonical form, CSV is the sectioned single file, XLSX is reserved.
 */

public enum TemplateFormat011 {

    /** JSON template (self-contained MetaDTO). */
    JSON("json"),

    /** CSV template (sectioned single file). */
    CSV("csv"),

    /** Markdown template (sectioned tables). */
    MARKDOWN("md"),

    /** XLSX template (reserved, not implemented yet). */
    XLSX("xlsx");

    /**
     * Format code.
     */
    private final String code;

    /**
     * Create the enum item.
     *
     * @param code format code
     */
    TemplateFormat011(String code) {
        this.code = code;
    }

    /**
     * Format code.
     *
     * @return code
     */
    public String getCode() {
        return code;
    }

    /**
     * Parse a format code case-insensitively.
     *
     * @param raw raw value, nullable
     * @return format or null when unknown/blank
     */
    public static TemplateFormat011 fromString(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String value = raw.trim().toLowerCase(Locale.ROOT);

        if ("markdown".equals(value)) {
            return MARKDOWN;
        }

        for (TemplateFormat011 format : values()) {
            if (format.code.equals(value)) {
                return format;
            }
        }

        return null;
    }
}
