package com.klsjnh.domain.datasource.kernel;

/*                SqlDialectPort011 interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  pluggable sql dialect SPI
 *
 */

/**
 * SPI: a pagination dialect for one database product. Implementations are
 * collected by the dialect resolver (Spring {@code List} injection), so a new
 * database is supported by adding a bean rather than editing the platform.
 * Dialects are stateless strategies, keyed by an open string {@code dbType}.
 */

public interface SqlDialectPort011 {

    /**
     * The database type code this dialect serves (open string vocabulary).
     *
     * @return normalized db type code, e.g. {@code mysql}
     */
    String dbType();

    /**
     * Build the page query: wrap the developer SQL and append the page clause.
     * Only code-computed numbers are concatenated.
     *
     * @param sql      developer select statement
     * @param offset   zero-based row offset
     * @param pageSize clamped page size
     * @return page sql
     */
    String pageSql(String sql, long offset, int pageSize);

    /**
     * Build the count query for the given developer SQL. The default wraps the
     * statement in a subquery; a database that forbids {@code ORDER BY} in a
     * derived table (SQL Server) overrides this.
     *
     * @param sql developer select statement
     * @return count sql
     */
    default String countSql(String sql) {
        return "SELECT COUNT(*) FROM ( " + sql + " ) klsjnh_count";
    }
}
