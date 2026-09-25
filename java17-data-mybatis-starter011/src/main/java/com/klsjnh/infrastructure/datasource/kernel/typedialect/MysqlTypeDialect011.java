package com.klsjnh.infrastructure.datasource.kernel.typedialect;

/*                MysqlTypeDialect011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  mysql column-type dialect
 *
 */

import com.klsjnh.common.constant.DatabaseTypes011;
import com.klsjnh.common.constant.FieldTypeCodes011;

import com.klsjnh.domain.datasource.kernel.ColumnMeta;
import com.klsjnh.domain.datasource.kernel.TypeDialectPort011;

import org.springframework.stereotype.Component;

/**
 * MySQL column-type dialect (backtick quoting; VARCHAR / BIGINT / DATETIME ...).
 */

@Component
public class MysqlTypeDialect011 implements TypeDialectPort011 {

    /**
     * Default character length.
     */
    private static final int DEFAULT_LENGTH = 255;

    /** {@inheritDoc} */
    @Override
    public String dbType() {
        return DatabaseTypes011.MYSQL;
    }

    /** {@inheritDoc} */
    @Override
    public String quote(String identifier) {
        return "`" + identifier + "`";
    }

    /** {@inheritDoc} */
    @Override
    public String columnType(ColumnMeta column) {
        switch (column.type()) {
            case FieldTypeCodes011.INT:
                return "INT";
            case FieldTypeCodes011.LONG:
                return "BIGINT";
            case FieldTypeCodes011.FLOAT:
                return "FLOAT";
            case FieldTypeCodes011.DOUBLE:
                return "DOUBLE";
            case FieldTypeCodes011.DECIMAL:
                return "DECIMAL(" + precision(column) + "," + scale(column) + ")";
            case FieldTypeCodes011.DATE:
                return "DATE";
            case FieldTypeCodes011.DATETIME:
                return "DATETIME";
            case FieldTypeCodes011.BOOLEAN:
                return "TINYINT(1)";
            case FieldTypeCodes011.TEXT:
                return "TEXT";
            case FieldTypeCodes011.BINARY:
                return "BLOB";
            case FieldTypeCodes011.JSON:
                return "JSON";
            default:
                return "VARCHAR(" + length(column) + ")";
        }
    }

    /**
     * Effective character length.
     *
     * @param column column metadata
     * @return length
     */
    private int length(ColumnMeta column) {
        return column.hasLength() ? column.length() : DEFAULT_LENGTH;
    }

    /**
     * Effective numeric precision.
     *
     * @param column column metadata
     * @return precision
     */
    private int precision(ColumnMeta column) {
        return column.precision() == null ? 18 : column.precision();
    }

    /**
     * Effective numeric scale.
     *
     * @param column column metadata
     * @return scale
     */
    private int scale(ColumnMeta column) {
        return column.scale() == null ? 2 : column.scale();
    }
}
