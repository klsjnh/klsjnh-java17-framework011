package com.klsjnh.application.lowcode011;

/*                MetadataValueValidator class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  metadata value validator class
 *
 */

import com.klsjnh.domain.lowcode011.JulyMetadataField;
import com.klsjnh.domain.lowcode011.enums.FieldType011;
import com.klsjnh.domain.lowcode011.records.BaseColumn011;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Metadata-driven value validation for runtime / open API writes: required,
 * type and length. A partial write (update) only validates the fields that are
 * present; a full write (insert) also enforces required fields.
 */

@Component
public class MetadataValueValidator {

    /**
     * Validate a write payload against the object fields.
     *
     * @param values  provided column values (lower-case keys)
     * @param fields  object fields
     * @param partial true for update (skip required on absent fields)
     * @return errors, empty when valid
     */
    public List<String> validate(Map<String, Object> values, List<JulyMetadataField> fields, boolean partial) {
        List<String> errors = new ArrayList<>();

        for (JulyMetadataField field : fields) {
            String code = field.fieldCode() == null ? "" : field.fieldCode().toLowerCase(Locale.ROOT);
            boolean present = values.containsKey(code);
            Object value = values.get(code);

            if (!present || value == null) {
                if (!partial && field.requiredField() && !isAuditColumn(code)) {
                    errors.add("required field missing: " + field.fieldCode());
                }
                continue;
            }

            String typeError = checkType(field, value);

            if (typeError != null) {
                errors.add(typeError);
            }
        }

        return errors;
    }

    /**
     * Type / length check for one value.
     *
     * @param field field
     * @param value raw value
     * @return error message, null when valid
     */
    private String checkType(JulyMetadataField field, Object value) {
        FieldType011 type = FieldType011.fromString(field.fieldType());
        String text = String.valueOf(value);

        if (type == null) {
            return null;
        }

        switch (type) {
            case INT:
            case BOOLEAN:
                return isInteger(text) || isBoolean(text) ? null : "not an integer/boolean: " + field.fieldCode();
            case FLOAT:
                return isDecimal(text) ? null : "not a decimal: " + field.fieldCode();
            case DATE:
            case CREATE_TIME:
            case UPDATE_TIME:
                return isDateLike(text) ? null : "not a date: " + field.fieldCode();
            default:
                return field.fieldLength() > 0 && text.length() > field.fieldLength()
                        ? "too long (max " + field.fieldLength() + "): " + field.fieldCode()
                        : null;
        }
    }

    /**
     * Whether the column is a platform-managed audit column.
     *
     * @param code lower-case code
     * @return true for audit-created columns
     */
    private boolean isAuditColumn(String code) {
        return BaseColumn011.AUDIT.contains(code);
    }

    /**
     * Whether the text parses as an integer.
     *
     * @param text text
     * @return true when integer
     */
    private boolean isInteger(String text) {
        try {
            Long.parseLong(text.trim());

            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    /**
     * Whether the text parses as a decimal.
     *
     * @param text text
     * @return true when decimal
     */
    private boolean isDecimal(String text) {
        try {
            new java.math.BigDecimal(text.trim());

            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    /**
     * Whether the text is a boolean-ish value.
     *
     * @param text text
     * @return true when boolean-ish
     */
    private boolean isBoolean(String text) {
        return "true".equalsIgnoreCase(text.trim()) || "false".equalsIgnoreCase(text.trim())
                || "0".equals(text.trim()) || "1".equals(text.trim());
    }

    /**
     * Lenient date check (ISO local date/time, space-separated, or epoch millis).
     *
     * @param text text
     * @return true when date-like
     */
    private boolean isDateLike(String text) {
        String t = text.trim();

        if (isInteger(t)) {
            return true;
        }

        return t.matches("\\d{4}-\\d{2}-\\d{2}([ T]\\d{2}:\\d{2}(:\\d{2})?(\\.\\d+)?)?");
    }
}
