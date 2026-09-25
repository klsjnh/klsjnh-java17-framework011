package com.klsjnh.domain.datasource.sync;

/*                Endpoint record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  sync endpoint record (source / target are symmetric)
 *
 */

/**
 * A sync endpoint: a datasource plus the data set read from / written to it.
 * Source and target are symmetric — internal object tables and external
 * databases are the same shape.
 *
 * @param dsCode datasource code (the primary datasource for internal objects)
 * @param kind   data set kind: {@code sql} / {@code table} / {@code object}
 * @param data   sql text / table name / object name
 */

public record Endpoint(String dsCode, String kind, String data) {

    /** SQL data set. */
    public static final String KIND_SQL = "sql";

    /** Table data set. */
    public static final String KIND_TABLE = "table";

    /** Internal object table data set. */
    public static final String KIND_OBJECT = "object";

    /**
     * Whether the endpoint reads from a SQL statement.
     *
     * @return true when the kind is sql
     */
    public boolean isSql() {
        return KIND_SQL.equalsIgnoreCase(kind);
    }
}
