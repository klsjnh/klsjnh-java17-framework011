package com.klsjnh.infrastructure.datasource.sync.writer;

/*                SqlserverWriterDialect011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.05
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.05  sqlserver writer dialect (MERGE upsert, HOLDLOCK)
 *
 */

import com.klsjnh.common.constant.DatabaseTypes011;

import com.klsjnh.domain.datasource.sync.WriterDialectPort;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.StringJoiner;

/**
 * SQL Server writer dialect: unquoted identifiers (whitelist-validated
 * upstream) and a {@code MERGE ... WITH (HOLDLOCK)} upsert terminated by a
 * semicolon (required for MERGE in T-SQL).
 */

@Component
public class SqlserverWriterDialect011 implements WriterDialectPort {

    /**
     * The database family served.
     *
     * @return sqlserver
     */
    @Override
    public String dbType() {
        return DatabaseTypes011.SQLSERVER;
    }

    /**
     * Build one INSERT (or MERGE) statement.
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

        if (!upsert || keyColumns == null || keyColumns.isEmpty()) {
            return "INSERT INTO " + table + " (" + columnList + ") VALUES (" + placeholders(columns.size()) + ")";
        }

        StringJoiner source = new StringJoiner(", ");
        StringJoiner on = new StringJoiner(" AND ");
        StringJoiner update = new StringJoiner(", ");
        StringJoiner insertColumns = new StringJoiner(", ");
        StringJoiner insertValues = new StringJoiner(", ");

        for (String column : columns) {
            source.add("? AS " + column);
            insertColumns.add(column);
            insertValues.add("src." + column);

            if (keyColumns.contains(column)) {
                on.add("dst." + column + " = src." + column);
            } else {
                update.add("dst." + column + " = src." + column);
            }
        }

        return "MERGE INTO " + table + " WITH (HOLDLOCK) AS dst USING (SELECT " + source + ") AS src ON (" + on + ")"
                + " WHEN MATCHED THEN UPDATE SET " + update
                + " WHEN NOT MATCHED THEN INSERT (" + insertColumns + ") VALUES (" + insertValues + ");";
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
