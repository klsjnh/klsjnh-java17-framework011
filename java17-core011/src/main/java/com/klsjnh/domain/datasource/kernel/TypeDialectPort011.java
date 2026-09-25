package com.klsjnh.domain.datasource.kernel;

/*                TypeDialectPort011 interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  pluggable column-type dialect SPI
 *
 */

import java.sql.Types;

/**
 * SPI: a column-type dialect for one database product. Sibling of
 * {@link SqlDialectPort011} (which handles read pagination); this one handles
 * JDBC↔neutral type mapping, target column types and value coercion.
 * Implementations are collected by a registry (Spring {@code List} injection),
 * so a new database is supported by adding a bean.
 */

public interface TypeDialectPort011 {

    /**
     * The database type code this dialect serves (open string vocabulary).
     *
     * @return normalized db type code, e.g. {@code mysql}
     */
    String dbType();

    /**
     * Render the target SQL column type for a neutral column.
     *
     * @param column column metadata (name / neutral type / length / precision / scale)
     * @return sql type clause, e.g. {@code VARCHAR(100)}
     */
    String columnType(ColumnMeta column);

    /**
     * Quote an identifier for this database (default: unchanged).
     *
     * @param identifier raw identifier
     * @return quoted identifier
     */
    default String quote(String identifier) {
        return identifier;
    }

    /**
     * Map a JDBC type to a neutral field type code. The default covers the
     * common {@link Types} values; a vendor overrides only what differs.
     *
     * @param jdbcType {@link Types} constant
     * @return neutral field type code
     */
    default String jdbcToNeutral(int jdbcType) {
        switch (jdbcType) {
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
                return "int";
            case Types.BIGINT:
                return "long";
            case Types.REAL:
            case Types.FLOAT:
                return "float";
            case Types.DOUBLE:
                return "double";
            case Types.NUMERIC:
            case Types.DECIMAL:
                return "decimal";
            case Types.DATE:
                return "date";
            case Types.TIME:
            case Types.TIMESTAMP:
            case Types.TIMESTAMP_WITH_TIMEZONE:
                return "datetime";
            case Types.BOOLEAN:
            case Types.BIT:
                return "boolean";
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
            case Types.BLOB:
                return "binary";
            case Types.CLOB:
            case Types.NCLOB:
            case Types.LONGVARCHAR:
            case Types.LONGNVARCHAR:
                return "text";
            default:
                return "string";
        }
    }

    /**
     * Coerce a value to the target neutral type before writing (default: pass
     * through unchanged).
     *
     * @param value     raw value
     * @param fieldType target neutral field type code
     * @return coerced value
     */
    default Object coerce(Object value, String fieldType) {
        return value;
    }
}
