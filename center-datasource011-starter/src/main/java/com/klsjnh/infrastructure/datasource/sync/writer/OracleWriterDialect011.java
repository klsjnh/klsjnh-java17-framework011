package com.klsjnh.infrastructure.datasource.sync.writer;

/*                OracleWriterDialect011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.05
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.05  oracle writer dialect (MERGE upsert)
 *
 */

import com.klsjnh.common.constant.DatabaseTypes011;

import com.klsjnh.domain.datasource.sync.WriterDialectPort;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.StringJoiner;

/**
 * Oracle writer dialect: unquoted identifiers (whitelist-validated upstream,
 * resolved with Oracle's default case rules) and a {@code MERGE} upsert over a
 * {@code FROM dual} source.
 */

@Component
public class OracleWriterDialect011 implements WriterDialectPort {

    /**
     * The database family served.
     *
     * @return oracle
     */
    @Override
    public String dbType() {
        return DatabaseTypes011.ORACLE;
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
            source.add("? " + column);
            insertColumns.add(column);
            insertValues.add("src." + column);

            if (keyColumns.contains(column)) {
                on.add("dst." + column + " = src." + column);
            } else {
                update.add("dst." + column + " = src." + column);
            }
        }

        return "MERGE INTO " + table + " dst USING (SELECT " + source + " FROM dual) src ON (" + on + ")"
                + " WHEN MATCHED THEN UPDATE SET " + update
                + " WHEN NOT MATCHED THEN INSERT (" + insertColumns + ") VALUES (" + insertValues + ")";
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
