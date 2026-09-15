package com.klsjnh.domain.lowcode011.model;

/*                FieldInfo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.11
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.11  field info 011 class
 *      2026.09.15  migrated to domain lowcode011, rebuilt as a setter-free entity
 *
 */

import com.klsjnh.common.util.StringUtil011;

/**
 * Low-code field definition: an entity inside the {@link MetaData011} aggregate
 * boundary, describing one business field of a low-code object.
 * <p>
 * Has no independent lifecycle — it is created, modified and removed only
 * through its aggregate root. Immutable identity ({@code fieldCode}), mutable
 * descriptive attributes, no setters: every change goes through an intent
 * method owned by the aggregate.
 * </p>
 */

public class FieldInfo011 {

    /**
     * Max length shared by the code and name attributes.
     */
    private static final int CODE_MAX = 60;

    /**
     * Field code (snake_case), unique within the aggregate, immutable after create.
     */
    private final String fieldCode;

    /**
     * Human-readable field name.
     */
    private String fieldName;

    /**
     * Field data type (string / int / float / date / boolean).
     */
    private String fieldType;

    /**
     * Maximum length for string fields, 0 for non-length types.
     */
    private int fieldLength;

    /**
     * Whether this field is required.
     */
    private boolean requiredField;

    /**
     * Default value when not provided, nullable.
     */
    private String defaultValue;

    /**
     * Full constructor, also the rehydration path from persistence.
     *
     * @param fieldCode     field code, unique within the aggregate
     * @param fieldName     human-readable field name
     * @param fieldType     field data type
     * @param fieldLength   maximum length, 0 for non-length types
     * @param requiredField whether this field is required
     * @param defaultValue  default value, nullable
     */
    public FieldInfo011(String fieldCode, String fieldName, String fieldType, int fieldLength,
            boolean requiredField, String defaultValue) {
        this.fieldCode = fieldCode;
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.fieldLength = fieldLength;
        this.requiredField = requiredField;
        this.defaultValue = defaultValue;
    }

    /**
     * Factory for a new field definition.
     *
     * @param fieldCode     field code, required, max 60
     * @param fieldName     human-readable field name, required, max 60
     * @param fieldType     field data type, required, max 30
     * @param fieldLength   maximum length, negative falls back to 0
     * @param requiredField whether this field is required
     * @param defaultValue  default value, nullable
     * @return new field definition
     */
    public static FieldInfo011 create(String fieldCode, String fieldName, String fieldType, int fieldLength,
            boolean requiredField, String defaultValue) {
        validateBasics(fieldCode, fieldName, fieldType);

        return new FieldInfo011(fieldCode, fieldName, fieldType, Math.max(fieldLength, 0), requiredField,
                defaultValue);
    }

    /**
     * Update the mutable descriptive attributes; the code is immutable.
     *
     * @param fieldName     human-readable field name, required, max 60
     * @param fieldType     field data type, required, max 30
     * @param fieldLength   maximum length, negative falls back to 0
     * @param requiredField whether this field is required
     * @param defaultValue  default value, nullable
     */
    public void updateBasics(String fieldName, String fieldType, int fieldLength, boolean requiredField,
            String defaultValue) {
        validateBasics(this.fieldCode, fieldName, fieldType);
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.fieldLength = Math.max(fieldLength, 0);
        this.requiredField = requiredField;
        this.defaultValue = defaultValue;
    }

    /**
     * Validate the mandatory attributes.
     *
     * @param code field code
     * @param name field name
     * @param type field data type
     */
    private static void validateBasics(String code, String name, String type) {
        if (StringUtil011.isMissing(code, CODE_MAX)) {
            throw new IllegalArgumentException("field code is required (max " + CODE_MAX + ")");
        }

        if (StringUtil011.isMissing(name, CODE_MAX)) {
            throw new IllegalArgumentException("field name is required (max " + CODE_MAX + ")");
        }

        if (StringUtil011.isMissing(type, 30)) {
            throw new IllegalArgumentException("field type is required (max 30)");
        }
    }

    /**
     * Get the field code.
     *
     * @return field code
     */
    public String fieldCode() {
        return fieldCode;
    }

    /**
     * Get the human-readable field name.
     *
     * @return field name
     */
    public String fieldName() {
        return fieldName;
    }

    /**
     * Get the field data type.
     *
     * @return field data type
     */
    public String fieldType() {
        return fieldType;
    }

    /**
     * Get the maximum length.
     *
     * @return maximum length, 0 for non-length types
     */
    public int fieldLength() {
        return fieldLength;
    }

    /**
     * Whether this field is required.
     *
     * @return true when required
     */
    public boolean requiredField() {
        return requiredField;
    }

    /**
     * Get the default value.
     *
     * @return default value, nullable
     */
    public String defaultValue() {
        return defaultValue;
    }
}
