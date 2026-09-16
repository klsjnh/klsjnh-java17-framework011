package com.klsjnh.domain.lowcode011;

/*                JulyMetadataField class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july metadata field class
 *
 */

import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.lowcode011.enums.FieldType011;

/**
 * Low-code core field definition (child of {@link JulyMetadata}).
 *
 * @param fieldCode     field code, unique within the object
 * @param fieldName     field display name
 * @param fieldType     field type code (FieldType011)
 * @param fieldLength   length, 0 for non-length types
 * @param requiredField whether the field is required
 * @param defaultValue  default value, nullable
 * @param sortOrder     manual sort order, null falls back to the default
 */

public record JulyMetadataField(String fieldCode, String fieldName, String fieldType, int fieldLength,
        boolean requiredField, String defaultValue, Integer sortOrder) {

    /**
     * Normalize and validate.
     */
    public JulyMetadataField {
        if (StringUtil011.isMissing(fieldCode, 60)) {
            throw new IllegalArgumentException("field code is required (max 60)");
        }

        if (StringUtil011.isMissing(fieldName, 60)) {
            throw new IllegalArgumentException("field name is required (max 60)");
        }

        if (!FieldType011.isKnown(fieldType)) {
            throw new IllegalArgumentException("unknown field type: " + fieldType);
        }

        if (fieldLength < 0) {
            fieldLength = 0;
        }

        if (sortOrder == null) {
            sortOrder = 9999;
        }
    }
}
