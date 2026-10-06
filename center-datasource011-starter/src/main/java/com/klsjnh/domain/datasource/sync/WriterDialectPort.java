package com.klsjnh.domain.datasource.sync;

/*                WriterDialectPort interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.05
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.05  writer dialect spi (mysql upsert / oracle & sqlserver merge)
 *
 */

import java.util.List;

/**
 * SPI: the upsert / insert statement builder of one target database family.
 * Implementations are collected by the writer dialect registry (Spring
 * {@code List} injection) and resolved by the target datasource type — MySQL
 * keeps {@code ON DUPLICATE KEY UPDATE}, Oracle / SQL Server use
 * {@code MERGE}, PostgreSQL uses {@code ON CONFLICT}.
 * <p>
 * Identifiers arriving here are already whitelist-validated
 * ({@code [A-Za-z0-9_]+}) by the sink; each dialect decides its own quoting.
 * Placeholder order MUST match the {@code columns} order — the sink binds row
 * values in that order.
 * </p>
 */

public interface WriterDialectPort {

    /**
     * The database family this dialect serves (normalized
     * {@code DatabaseTypes011} code).
     *
     * @return database type code
     */
    String dbType();

    /**
     * Build one INSERT (or upsert) statement for a row.
     *
     * @param table      target table name (whitelist-validated identifier)
     * @param columns    target column names, whitelist-validated, never empty
     * @param keyColumns business key columns (subset of columns), may be empty
     * @param upsert     true when the conflict strategy is upsert (requires
     *                   non-empty keyColumns)
     * @return one statement with one {@code ?} per column, in columns order
     */
    String buildInsert(String table, List<String> columns, List<String> keyColumns, boolean upsert);
}
