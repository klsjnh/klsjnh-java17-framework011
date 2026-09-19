package com.klsjnh.infrastructure.datasource.kernel;

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
 *      2026.09.19  open provider registry (list of SqlDialectPort011)
 *
 */

import com.klsjnh.common.constant.DatabaseTypes011;
import com.klsjnh.common.exception.BusinessException;

import com.klsjnh.domain.datasource.kernel.SqlDialectPort011;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pagination dialect resolver: looks up an open registry of
 * {@link SqlDialectPort011} beans by the target database type. Only code-computed
 * numbers (offset / size) are concatenated — never user strings. A new database
 * is supported by adding a dialect bean; the platform is not modified.
 */

@Component
public class SqlDialect011 {

    /**
     * Normalized db type to dialect index, built from every injected dialect bean.
     */
    private final Map<String, SqlDialectPort011> byType = new HashMap<>();

    /**
     * Create the resolver from all dialect beans on the classpath.
     *
     * @param dialects pagination dialects
     */
    public SqlDialect011(List<SqlDialectPort011> dialects) {
        for (SqlDialectPort011 dialect : dialects) {
            byType.put(DatabaseTypes011.normalize(dialect.dbType()), dialect);
        }
    }

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
        String type = DatabaseTypes011.normalize(dsType);
        SqlDialectPort011 dialect = type == null ? null : byType.get(type);

        if (dialect == null) {
            throw BusinessException.badRequest("unknown database type: " + dsType);
        }

        return dialect.pageSql(sql, offset, pageSize);
    }

    /**
     * Build the count query (database agnostic).
     *
     * @param sql developer select statement
     * @return count sql
     */
    public String countSql(String sql) {
        return "SELECT COUNT(*) FROM ( " + sql + " ) klsjnh_count";
    }
}
