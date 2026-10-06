package com.klsjnh.infrastructure.datasource.sync.writer;

/*                PostgresqlWriterDialect011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.05
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.05  postgresql writer dialect (ON CONFLICT upsert)
 *
 */

import com.klsjnh.common.constant.DatabaseTypes011;

import com.klsjnh.domain.datasource.sync.WriterDialectPort;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.StringJoiner;

/**
 * PostgreSQL writer dialect: unquoted identifiers (whitelist-validated
 * upstream) and an {@code INSERT ... ON CONFLICT} upsert — the business key
 * must be backed by a unique index on the target table.
 */

@Component
public class PostgresqlWriterDialect011 implements WriterDialectPort {

    /**
     * The database family served.
     *
     * @return postgresql
     */
    @Override
    public String dbType() {
        return DatabaseTypes011.POSTGRESQL;
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
        String columnList = join(columns);
        String sql = "INSERT INTO " + table + " (" + columnList + ") VALUES (" + placeholders(columns.size()) + ")";

        if (upsert && keyColumns != null && !keyColumns.isEmpty()) {
            StringJoiner keys = new StringJoiner(", ");
            StringJoiner updates = new StringJoiner(", ");
            for (String key : keyColumns) {
                keys.add(key);
            }
            for (String column : columns) {
                if (!keyColumns.contains(column)) {
                    updates.add(column + " = EXCLUDED." + column);
                }
            }
            sql += " ON CONFLICT (" + keys + ") DO UPDATE SET " + updates;
        }

        return sql;
    }

    /**
     * Join identifiers.
     *
     * @param names identifiers
     * @return joined list
     */
    private String join(List<String> names) {
        StringJoiner joiner = new StringJoiner(", ");

        for (String name : names) {
            joiner.add(name);
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
