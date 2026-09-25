package com.klsjnh.infrastructure.datasource.kernel.typedialect;

/*                PostgresqlTypeDialect011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  postgresql column-type dialect
 *
 */

import com.klsjnh.common.constant.DatabaseTypes011;
import com.klsjnh.common.constant.FieldTypeCodes011;

import com.klsjnh.domain.datasource.kernel.ColumnMeta;
import com.klsjnh.domain.datasource.kernel.TypeDialectPort011;

import org.springframework.stereotype.Component;

/**
 * PostgreSQL column-type dialect (double-quote quoting; VARCHAR / BIGINT /
 * TIMESTAMP ...).
 */

@Component
public class PostgresqlTypeDialect011 implements TypeDialectPort011 {

    /**
     * Default character length.
     */
    private static final int DEFAULT_LENGTH = 255;

    /** {@inheritDoc} */
    @Override
    public String dbType() {
        return DatabaseTypes011.POSTGRESQL;
    }

    /** {@inheritDoc} */
    @Override
    public String quote(String identifier) {
        return "\"" + identifier + "\"";
    }

    /** {@inheritDoc} */
    @Override
    public String columnType(ColumnMeta column) {
        switch (column.type()) {
            case FieldTypeCodes011.INT:
                return "INTEGER";
            case FieldTypeCodes011.LONG:
                return "BIGINT";
            case FieldTypeCodes011.FLOAT:
                return "REAL";
            case FieldTypeCodes011.DOUBLE:
                return "DOUBLE PRECISION";
            case FieldTypeCodes011.DECIMAL:
                return "NUMERIC(" + (column.precision() == null ? 18 : column.precision()) + ","
                        + (column.scale() == null ? 2 : column.scale()) + ")";
            case FieldTypeCodes011.DATE:
                return "DATE";
            case FieldTypeCodes011.DATETIME:
                return "TIMESTAMP";
            case FieldTypeCodes011.BOOLEAN:
                return "BOOLEAN";
            case FieldTypeCodes011.TEXT:
                return "TEXT";
            case FieldTypeCodes011.BINARY:
                return "BYTEA";
            case FieldTypeCodes011.JSON:
                return "JSONB";
            default:
                return "VARCHAR(" + (column.hasLength() ? column.length() : DEFAULT_LENGTH) + ")";
        }
    }
}
