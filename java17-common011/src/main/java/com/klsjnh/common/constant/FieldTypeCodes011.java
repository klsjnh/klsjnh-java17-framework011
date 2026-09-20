package com.klsjnh.common.constant;

/*                FieldTypeCodes011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  neutral field type codes (open string vocabulary)
 *
 */

/**
 * Neutral field type codes — the shared vocabulary for the data source center's
 * type contract ({@code docs/infrastructure011/017.datasource-center}). Plain
 * string constants, not an enum: every database JDBC type and every product
 * field type maps to these codes.
 */

public final class FieldTypeCodes011 {

    /** Variable-length string. */
    public static final String STRING = "string";

    /** 32-bit integer. */
    public static final String INT = "int";

    /** 64-bit integer. */
    public static final String LONG = "long";

    /** Single precision float. */
    public static final String FLOAT = "float";

    /** Double precision float. */
    public static final String DOUBLE = "double";

    /** Fixed point decimal. */
    public static final String DECIMAL = "decimal";

    /** Date. */
    public static final String DATE = "date";

    /** Date and time. */
    public static final String DATETIME = "datetime";

    /** Boolean. */
    public static final String BOOLEAN = "boolean";

    /** Long text. */
    public static final String TEXT = "text";

    /** Binary. */
    public static final String BINARY = "binary";

    /** JSON. */
    public static final String JSON = "json";

    private FieldTypeCodes011() {
    }
}
