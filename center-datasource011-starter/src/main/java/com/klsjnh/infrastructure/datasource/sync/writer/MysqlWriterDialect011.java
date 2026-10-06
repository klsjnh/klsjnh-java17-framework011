package com.klsjnh.infrastructure.datasource.sync.writer;

/*                MysqlWriterDialect011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.05
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.05  mysql writer dialect (extracted from SyncSink011)
 *
 */

import com.klsjnh.common.constant.DatabaseTypes011;

import com.klsjnh.domain.datasource.sync.WriterDialectPort;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.StringJoiner;

/**
 * MySQL writer dialect: backtick-quoted identifiers and
 * {@code INSERT ... ON DUPLICATE KEY UPDATE} upsert (the original SyncSink
 * statement shape, extracted verbatim).
 */

@Component
public class MysqlWriterDialect011 implements WriterDialectPort {

    /**
     * The database family served.
     *
     * @return mysql
     */
    @Override
    public String dbType() {
        return DatabaseTypes011.MYSQL;
    }

    /**
     * Build one INSERT (or upsert) statement.
     *
     * @param table      target table name
     * @param columns    target column names
     * @param keyColumns business key columns
     * @param upsert     true when upsert is requested
     * @return one statement with one {@code ?} per column
     */
    @Override
    public String buildInsert(String table, List<String> columns, List<String> keyColumns, boolean upsert) {
        String columnList = joinQuoted(columns);
        String sql = "INSERT INTO `" + table + "` (" + columnList + ") VALUES (" + placeholders(columns.size()) + ")";

        if (upsert && keyColumns != null && !keyColumns.isEmpty()) {
            StringJoiner updates = new StringJoiner(", ");
            for (String column : columns) {
                updates.add("`" + column + "` = VALUES(`" + column + "`)");
            }
            sql += " ON DUPLICATE KEY UPDATE " + updates;
        }

        return sql;
    }

    /**
     * Join identifiers with backticks.
     *
     * @param names identifiers
     * @return joined list
     */
    private String joinQuoted(List<String> names) {
        StringJoiner joiner = new StringJoiner(", ");

        for (String name : names) {
            joiner.add("`" + name + "`");
        }

        return joiner.toString();
    }

    /**
     * Repeat {@code ?} placeholders.
     *
     * @param count placeholder count
     * @return joined placeholders
     */
    private String placeholders(int count) {
        StringJoiner joiner = new StringJoiner(", ");

        for (int i = 0; i < count; i++) {
            joiner.add("?");
        }

        return joiner.toString();
    }
}
