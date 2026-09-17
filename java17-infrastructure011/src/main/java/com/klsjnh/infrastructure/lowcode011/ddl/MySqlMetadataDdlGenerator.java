package com.klsjnh.infrastructure.lowcode011.ddl;

/*                MySqlMetadataDdlGenerator class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  mysql metadata ddl generator class
 *
 */

import com.klsjnh.domain.lowcode011.JulyMetadataField;
import com.klsjnh.domain.lowcode011.MetadataDdlGeneratorPort;
import com.klsjnh.domain.lowcode011.enums.FieldType011;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * MySQL DDL generator for low-code objects. Identifiers are strictly validated
 * (never interpolated blindly) and field names are escaped into comments. The
 * same generator backs preview and publish.
 */

@Component
public class MySqlMetadataDdlGenerator implements MetadataDdlGeneratorPort {

    /**
     * Accepted identifier shape: letter/underscore start, 60 chars max.
     */
    private static final Pattern IDENTIFIER = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]{0,59}$");

    /**
     * Upper bound for inferred VARCHAR lengths.
     */
    private static final int MAX_VARCHAR = 2000;

    /**
     * Generate the CREATE TABLE statement.
     *
     * @param physicalTable physical table name (already prefixed)
     * @param tableComment  table comment
     * @param fields        object fields
     * @return CREATE TABLE statement (without trailing semicolon)
     */
    @Override
    public String generateCreate(String physicalTable, String tableComment, List<JulyMetadataField> fields) {
        if (fields == null || fields.isEmpty()) {
            throw new IllegalArgumentException("metadata has no fields to publish");
        }

        String table = requireIdentifier(physicalTable);
        List<String> lines = new ArrayList<>();
        String primaryKey = null;

        for (JulyMetadataField field : fields) {
            String column = requireIdentifier(field.fieldCode());
            FieldType011 type = requireType(field);

            if (type == FieldType011.ID && primaryKey == null) {
                primaryKey = column;
            }

            lines.add(columnLine(field, type));
        }

        if (primaryKey == null) {
            primaryKey = "id";
            lines.add(0, "  `id` VARCHAR(33) NOT NULL COMMENT 'primary key'");
        }

        lines.add("  PRIMARY KEY (`" + primaryKey + "`)");

        return "CREATE TABLE IF NOT EXISTS `" + table + "` (\n" + String.join(",\n", lines)
                + "\n) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='"
                + escapeLiteral(tableComment == null ? table : tableComment) + "'";
    }

    /**
     * Generate an ALTER TABLE ... ADD COLUMN statement (ADD only).
     *
     * @param physicalTable physical table name
     * @param fields        fields to add
     * @return ALTER TABLE statement (without trailing semicolon)
     */
    @Override
    public String generateAddColumns(String physicalTable, List<JulyMetadataField> fields) {
        if (fields == null || fields.isEmpty()) {
            throw new IllegalArgumentException("no fields to add");
        }

        String table = requireIdentifier(physicalTable);
        List<String> clauses = new ArrayList<>();

        for (JulyMetadataField field : fields) {
            clauses.add("ADD COLUMN " + columnLine(field, requireType(field)).trim());
        }

        return "ALTER TABLE `" + table + "`\n  " + String.join(",\n  ", clauses);
    }

    /**
     * Build one column definition line.
     *
     * @param field field
     * @param type  resolved field type
     * @return column definition (indented)
     */
    private String columnLine(JulyMetadataField field, FieldType011 type) {
        String column = requireIdentifier(field.fieldCode());
        String nullable = field.requiredField() ? " NOT NULL" : "";
        String comment = field.fieldName() == null ? field.fieldCode() : field.fieldName();

        return "  `" + column + "` " + columnType(type, field.fieldLength()) + nullable + " COMMENT '"
                + escapeLiteral(comment) + "'";
    }

    /**
     * Resolve and validate a field's type.
     *
     * @param field field
     * @return field type
     */
    private FieldType011 requireType(JulyMetadataField field) {
        FieldType011 type = FieldType011.fromString(field.fieldType());

        if (type == null) {
            throw new IllegalArgumentException("unknown field type: " + field.fieldType());
        }

        return type;
    }

    /**
     * Map a field type to a MySQL column type.
     *
     * @param type   field type
     * @param length declared length
     * @return column type
     */
    private String columnType(FieldType011 type, int length) {
        switch (type) {
            case ID:
                return "VARCHAR(33)";
            case STATUS:
                return "VARCHAR(3)";
            case CREATE_BY:
            case UPDATE_BY:
                return "VARCHAR(33)";
            case CREATE_TIME:
            case UPDATE_TIME:
            case DATE:
                return "DATETIME";
            case INT:
                return "INT";
            case FLOAT:
                return "DECIMAL(18,4)";
            case BOOLEAN:
                return "TINYINT(1)";
            case TEXT:
                return "TEXT";
            default:
                int len = length > 0 && length <= MAX_VARCHAR ? length : 255;

                return "VARCHAR(" + len + ")";
        }
    }

    /**
     * Validate an identifier strictly.
     *
     * @param value raw identifier
     * @return trimmed identifier
     */
    private String requireIdentifier(String value) {
        if (value == null || !IDENTIFIER.matcher(value.trim()).matches()) {
            throw new IllegalArgumentException("invalid identifier: " + value);
        }

        return value.trim();
    }

    /**
     * Escape a single-quoted literal.
     *
     * @param value raw literal
     * @return escaped literal
     */
    private String escapeLiteral(String value) {
        return value == null ? "" : value.replace("'", "''");
    }
}
