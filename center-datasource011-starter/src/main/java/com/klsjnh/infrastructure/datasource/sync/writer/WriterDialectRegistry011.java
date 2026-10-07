package com.klsjnh.infrastructure.datasource.sync.writer;

/*                WriterDialectRegistry011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.05
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.05  writer dialect registry (dbType -> dialect, mysql default)
 *
 */

import com.klsjnh.common.constant.DatabaseTypes011;

import com.klsjnh.domain.datasource.sync.WriterDialectPort;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Collects every {@link WriterDialectPort} bean into a dbType-to-dialect map
 * (immutable snapshot at startup). Resolution falls back to the MySQL dialect
 * — the S1 local-table convention — so a null / unknown target type still
 * writes.
 */

@Component
public class WriterDialectRegistry011 {

    /**
     * Database type to dialect mapping.
     */
    private final Map<String, WriterDialectPort> dialects;

    /**
     * Create the registry from all writer dialect beans.
     *
     * @param dialects writer dialect beans found in the context
     */
    public WriterDialectRegistry011(List<WriterDialectPort> dialects) {
        this.dialects = dialects.stream()
                .collect(Collectors.toUnmodifiableMap(WriterDialectPort::dbType, Function.identity()));
    }

    /**
     * Resolve a dialect by database type; unknown / null types fall back to
     * the MySQL dialect.
     *
     * @param dbType raw database type, nullable
     * @return dialect, never null
     */
    public WriterDialectPort resolve(String dbType) {
        String type = DatabaseTypes011.normalize(dbType);

        if (type != null) {
            WriterDialectPort dialect = dialects.get(type);
            if (dialect != null) {
                return dialect;
            }
        }

        WriterDialectPort fallback = dialects.get(DatabaseTypes011.MYSQL);

        if (fallback == null) {
            throw new IllegalStateException("no mysql writer dialect registered");
        }

        return fallback;
    }
}
