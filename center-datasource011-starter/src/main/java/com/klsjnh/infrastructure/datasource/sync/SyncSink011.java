package com.klsjnh.infrastructure.datasource.sync;

/*                SyncSink011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate 2026.10.05
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  sync sink (upsert into the local/primary table)
 *      2026.10.05  statement building moved to WriterDialectPort (per dbType)
 *
 */

import com.klsjnh.common.exception.BusinessException;

import com.klsjnh.domain.datasource.sync.Endpoint;
import com.klsjnh.domain.datasource.sync.SyncSinkPort;
import com.klsjnh.domain.datasource.sync.WriterDialectPort;
import com.klsjnh.domain.datasource.kernel.ConnectionInfo;
import com.klsjnh.domain.datasource.kernel.DynamicDataSourceRegistryPort;

import com.klsjnh.infrastructure.datasource.sync.writer.WriterDialectRegistry011;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Sync sink implementation: writes mapped rows into the LOCAL (primary) table.
 * Identifiers are validated (letters / digits / underscore); the statement
 * shape (quoting + upsert clause) comes from the
 * {@link WriterDialectPort writer dialect} resolved by the target datasource
 * type — null / unknown falls back to MySQL (the S1 local-table convention).
 * Upsert uses the dialect's native clause (ON DUPLICATE KEY / MERGE / ON
 * CONFLICT) on the business key.
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
     * Writer dialect registry (dbType -> statement builder).
     */
    private final WriterDialectRegistry011 writerDialects;

    /**
     * Dynamic datasource registry (resolves the target ds type, nullable-safe).
     */
    private final DynamicDataSourceRegistryPort registry;

    /**
     * Create the sink.
     *
     * @param primaryDataSource the auto-configured primary datasource
     * @param registry          dynamic datasource registry
     * @param writerDialects    writer dialect registry
     */
    public SyncSink011(DataSource primaryDataSource, DynamicDataSourceRegistryPort registry,
            WriterDialectRegistry011 writerDialects) {
        this.jdbcTemplate = new JdbcTemplate(primaryDataSource);
        this.registry = registry;
        this.writerDialects = writerDialects;
    }

    /** {@inheritDoc} */
    @Override
    public int writePage(Endpoint endpoint, String targetTable, List<String> keyColumns, boolean upsert,
            List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return 0;
        }

        String table = validate(targetTable, "table");
        List<String> keys = validateKeys(keyColumns, upsert);
        WriterDialectPort dialect = writerDialects.resolve(resolveDbType(endpoint));
        int written = 0;

        for (Map<String, Object> row : rows) {
            List<String> columns = new ArrayList<>(row.keySet());
            List<Object> values = new ArrayList<>();

            for (String column : columns) {
                validate(column, "column");
                values.add(row.get(column));
            }

            if (upsert) {
                // Degenerate MERGE / ON CONFLICT shapes are configuration
                // errors: fail fast here instead of at every dialect tick.
                if (!columns.containsAll(keys)) {
                    throw BusinessException.badRequest(
                            "sync sink: syncKey " + keys + " is not mapped in the row columns " + columns);
                }
                if (columns.size() == keys.size()) {
                    throw BusinessException.badRequest("sync sink: no non-key column to update on conflict");
                }
            }

            String sql = dialect.buildInsert(table, columns, keys, upsert);

            jdbcTemplate.update(sql, values.toArray());
            written++;
        }

        return written;
    }

    /**
     * Validate the business key columns: every key must be a whitelist
     * identifier (dialects concatenate keys into ON / CONFLICT clauses), and
     * upsert requires at least one key.
     *
     * @param keyColumns business key columns, nullable
     * @param upsert     conflict strategy
     * @return validated key list (empty for append)
     */
    private List<String> validateKeys(List<String> keyColumns, boolean upsert) {
        if (keyColumns == null || keyColumns.isEmpty()) {
            if (upsert) {
                throw BusinessException.badRequest("sync sink: syncKey is required for upsert");
            }
            return List.of();
        }

        List<String> keys = new ArrayList<>();

        for (String key : keyColumns) {
            keys.add(validate(key, "syncKey"));
        }

        return keys;
    }

    /**
     * Resolve the target database type from the endpoint: a declared dynamic
     * datasource carries its own type, anything else (local / primary target)
     * resolves to the registry's MySQL fallback.
     *
     * @param endpoint target endpoint
     * @return raw database type, nullable
     */
    private String resolveDbType(Endpoint endpoint) {
        String dsCode = endpoint == null ? null : endpoint.dsCode();

        if (dsCode == null || dsCode.isBlank() || "master".equals(dsCode)) {
            return null;
        }

        ConnectionInfo info = registry.configOf(dsCode);

        return info == null ? null : info.dsType();
    }

    /**
     * Validate an identifier (letters / digits / underscore only) — the
     * dialects quote or emit it verbatim, so nothing else can slip through.
     *
     * @param name identifier
     * @param kind kind label for the error message
     * @return the validated identifier
     */
    private String validate(String name, String kind) {
        if (name == null || !name.matches(IDENTIFIER)) {
            throw BusinessException.badRequest("illegal sql identifier (" + kind + "): " + name);
        }

        return name;
    }
}
