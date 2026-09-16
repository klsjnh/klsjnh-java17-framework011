package com.klsjnh.domain.lowcode011.enums;

/*                FieldType011 enum
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  field type 011 enum
 *
 */

import java.util.Locale;

/**
 * Low-code field type value object: classifies one field of a low-code object
 * by the column shape the generator must produce.
 * <p>
 * The code is the persistence-facing value and the label is the
 * presentation-facing display name. Immutable and compared by value.
 * </p>
 * <p>
 * Layout of the twelve codes (project convention, see the business modeling
 * topic 033 section 015.4):
 * </p>
 * <ul>
 *   <li>Structural codes, one per common column of
 *       {@code base-entity-columns.sql} — {@code id}, {@code status},
 *       {@code create_by}, {@code update_by}, {@code create_time},
 *       {@code update_time}. They tell the generator that the column is a
 *       read-only technical column, not an editable business input.
 *       {@code dr} shares the {@code status} code: both are VARCHAR(3)
 *       technical markers and carry the same shape.</li>
 *   <li>Business codes — {@code string}, {@code int}, {@code float},
 *       {@code date}, {@code boolean}, {@code text}.</li>
 * </ul>
 * <p>
 * This enum is the single validation authority for
 * {@code FieldInfo011.fieldType}, which itself stays a plain String: the field
 * attribute is deliberately not tightened to the enum type, so rehydrating
 * metadata from an external source is never blocked by an unknown code.
 * Validation happens at the boundary — see
 * {@code com.klsjnh.domain.dataservice011}.
 * </p>
 */

public enum FieldType011 {

    /** Primary key column. */
    ID("id", "primary key"),

    /** Row status and delete marker column. */
    STATUS("status", "status"),

    /** Creator audit column. */
    CREATE_BY("create_by", "creator"),

    /** Last modifier audit column. */
    UPDATE_BY("update_by", "last modifier"),

    /** Creation time audit column. */
    CREATE_TIME("create_time", "creation time"),

    /** Last update time audit column. */
    UPDATE_TIME("update_time", "last update time"),

    /** Variable length character data. */
    STRING("string", "string"),

    /** Whole number. */
    INT("int", "integer"),

    /** Decimal number. */
    FLOAT("float", "decimal"),

    /** Date and time. */
    DATE("date", "date time"),

    /** Boolean flag. */
    BOOLEAN("boolean", "boolean"),

    /** Long text. */
    TEXT("text", "long text");

    /**
     * Persistence code, string.
     */
    private final String code;

    /**
     * Display label, string.
     */
    private final String label;

    /**
     * Create a field type constant.
     *
     * @param code  persistence code
     * @param label display label
     */
    FieldType011(String code, String label) {
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
    public static FieldType011 fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String v = value.trim().toLowerCase(Locale.ROOT);

        for (FieldType011 type : values()) {
            if (type.code.equals(v)) {
                return type;
            }
        }

        return null;
    }

    /**
     * Whether the raw code resolves to a known field type.
     *
     * @param value raw code
     * @return true when a matching constant exists
     */
    public static boolean isKnown(String value) {
        return fromString(value) != null;
    }
}
