package com.klsjnh.infrastructure.datasource.kernel.typedialect;

/*                SqlserverTypeDialect011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  sqlserver column-type dialect
 *
 */

import com.klsjnh.common.constant.DatabaseTypes011;
import com.klsjnh.common.constant.FieldTypeCodes011;

import com.klsjnh.domain.datasource.kernel.ColumnMeta;
import com.klsjnh.domain.datasource.kernel.TypeDialectPort011;

import org.springframework.stereotype.Component;

/**
 * SQL Server column-type dialect (bracket quoting; NVARCHAR / BIGINT /
 * DATETIME2 / VARBINARY(MAX) ...).
 */

@Component
public class SqlserverTypeDialect011 implements TypeDialectPort011 {

    /**
     * Default character length.
     */
    private static final int DEFAULT_LENGTH = 255;

    /** {@inheritDoc} */
    @Override
    public String dbType() {
        return DatabaseTypes011.SQLSERVER;
    }

    /** {@inheritDoc} */
    @Override
    public String quote(String identifier) {
        return "[" + identifier + "]";
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
                return "REAL";
            case FieldTypeCodes011.DOUBLE:
                return "FLOAT";
            case FieldTypeCodes011.DECIMAL:
                return "DECIMAL(" + (column.precision() == null ? 18 : column.precision()) + ","
                        + (column.scale() == null ? 2 : column.scale()) + ")";
            case FieldTypeCodes011.DATE:
                return "DATE";
            case FieldTypeCodes011.DATETIME:
                return "DATETIME2";
            case FieldTypeCodes011.BOOLEAN:
                return "BIT";
            case FieldTypeCodes011.TEXT:
                return "NVARCHAR(MAX)";
            case FieldTypeCodes011.BINARY:
                return "VARBINARY(MAX)";
            case FieldTypeCodes011.JSON:
                return "NVARCHAR(MAX)";
            default:
                return "NVARCHAR(" + (column.hasLength() ? column.length() : DEFAULT_LENGTH) + ")";
        }
    }
}
