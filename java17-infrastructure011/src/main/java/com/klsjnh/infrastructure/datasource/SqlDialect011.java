package com.klsjnh.infrastructure.datasource;

/*                SqlDialect011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  sql dialect 011 class
 *
 */

import com.klsjnh.common.enums.DatabaseType011;
import com.klsjnh.common.exception.BusinessException;

import org.springframework.stereotype.Component;

/**
 * Pagination dialect strategy: wraps the developer SQL as a subquery and
 * appends the page clause of the target database. Only literal numbers
 * (offset / size, both code-computed) are concatenated — never user strings.
 */

@Component
public class SqlDialect011 {

    /**
     * Build the page query for a database type.
     *
     * @param dsType   database type (mysql / postgresql / oracle / sqlserver)
     * @param sql      developer select statement
     * @param offset   zero-based row offset
     * @param pageSize clamped page size
     * @return page sql
     */
    public String pageSql(String dsType, String sql, long offset, int pageSize) {
        DatabaseType011 type = DatabaseType011.fromString(dsType);

        if (type == null) {
            throw BusinessException.badRequest("unknown database type: " + dsType);
        }

        return switch (type) {
            case MYSQL, POSTGRESQL ->
                    "SELECT * FROM ( " + sql + " ) klsjnh_page LIMIT " + pageSize + " OFFSET " + offset;
            case ORACLE, SQLSERVER ->
                    "SELECT * FROM ( " + sql + " ) klsjnh_page OFFSET " + offset + " ROWS FETCH NEXT "
                            + pageSize + " ROWS ONLY";
        };
    }

    /**
     * Build the count query for a database type.
     *
     * @param sql developer select statement
     * @return count sql
     */
    public String countSql(String sql) {
        return "SELECT COUNT(*) FROM ( " + sql + " ) klsjnh_count";
    }
}
