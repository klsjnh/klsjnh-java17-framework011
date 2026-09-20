package com.klsjnh.infrastructure.datasource.kernel.typedialect;

/*                OracleTypeDialect011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  oracle column-type dialect
 *
 */

import com.klsjnh.common.constant.DatabaseTypes011;
import com.klsjnh.common.constant.FieldTypeCodes011;

import com.klsjnh.domain.datasource.kernel.ColumnMeta;
import com.klsjnh.domain.datasource.kernel.TypeDialectPort011;

import org.springframework.stereotype.Component;

/**
 * Oracle column-type dialect (double-quote quoting; VARCHAR2 / NUMBER /
 * TIMESTAMP / CLOB ...).
 */

@Component
public class OracleTypeDialect011 implements TypeDialectPort011 {

    /**
     * Default character length.
     */
    private static final int DEFAULT_LENGTH = 255;

    /** {@inheritDoc} */
    @Override
    public String dbType() {
        return DatabaseTypes011.ORACLE;
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
                return "NUMBER(10)";
            case FieldTypeCodes011.LONG:
                return "NUMBER(19)";
            case FieldTypeCodes011.FLOAT:
                return "BINARY_FLOAT";
            case FieldTypeCodes011.DOUBLE:
                return "BINARY_DOUBLE";
            case FieldTypeCodes011.DECIMAL:
                return "NUMBER(" + (column.precision() == null ? 18 : column.precision()) + ","
                        + (column.scale() == null ? 2 : column.scale()) + ")";
            case FieldTypeCodes011.DATE:
                return "DATE";
            case FieldTypeCodes011.DATETIME:
                return "TIMESTAMP";
            case FieldTypeCodes011.BOOLEAN:
                return "NUMBER(1)";
            case FieldTypeCodes011.TEXT:
                return "CLOB";
            case FieldTypeCodes011.BINARY:
                return "BLOB";
            case FieldTypeCodes011.JSON:
                return "CLOB";
            default:
                return "VARCHAR2(" + (column.hasLength() ? column.length() : DEFAULT_LENGTH) + ")";
        }
    }
}
