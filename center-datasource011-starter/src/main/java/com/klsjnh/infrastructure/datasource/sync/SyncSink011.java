package com.klsjnh.infrastructure.datasource.sync;

/*                SyncSink011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  sync sink (upsert into the local/primary table)
 *
 */

import com.klsjnh.domain.datasource.sync.Endpoint;
import com.klsjnh.domain.datasource.sync.SyncSinkPort;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Sync sink implementation: writes mapped rows into the LOCAL (primary) table.
 * Identifiers are validated (letters / digits / underscore) and quoted with
 * backticks (MySQL primary), so a table or column name can never inject SQL.
 * Upsert uses {@code INSERT ... ON DUPLICATE KEY UPDATE} on the business key's
 * unique index.
 */

@Component
public class SyncSink011 implements SyncSinkPort {

    /**
     * Identifier pattern.
     */
    private static final String IDENTIFIER = "[A-Za-z0-9_]+";

    /**
     * JDBC template over the primary datasource.
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * Create the sink.
     *
     * @param primaryDataSource the auto-configured primary datasource
     */
    public SyncSink011(DataSource primaryDataSource) {
        this.jdbcTemplate = new JdbcTemplate(primaryDataSource);
    }

    /** {@inheritDoc} */
    @Override
    public int writePage(Endpoint endpoint, String targetTable, List<String> keyColumns, boolean upsert,
            List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return 0;
        }

        String table = quote(targetTable);
        int written = 0;

        for (Map<String, Object> row : rows) {
            List<String> columns = new ArrayList<>(row.keySet());
            List<Object> values = new ArrayList<>();

            StringBuilder columnList = new StringBuilder();
            StringBuilder placeholders = new StringBuilder();

            for (String column : columns) {
                if (columnList.length() > 0) {
                    columnList.append(", ");
                    placeholders.append(", ");
                }
                columnList.append(quote(column));
                placeholders.append("?");
                values.add(row.get(column));
            }

            StringBuilder sql = new StringBuilder("INSERT INTO ").append(table).append(" (").append(columnList)
                    .append(") VALUES (").append(placeholders).append(")");

            if (upsert && keyColumns != null && !keyColumns.isEmpty()) {
                sql.append(" ON DUPLICATE KEY UPDATE ");
                for (int i = 0; i < columns.size(); i++) {
                    if (i > 0) {
                        sql.append(", ");
                    }
                    String column = quote(columns.get(i));
                    sql.append(column).append(" = VALUES(").append(column).append(")");
                }
            }

            jdbcTemplate.update(sql.toString(), values.toArray());
            written++;
        }

        return written;
    }

    /**
     * Validate and quote an identifier.
     *
     * @param name identifier
     * @return quoted identifier
     */
    private String quote(String name) {
        if (name == null || !name.matches(IDENTIFIER)) {
            throw new IllegalStateException("illegal sql identifier: " + name);
        }

        return "`" + name + "`";
    }
}
