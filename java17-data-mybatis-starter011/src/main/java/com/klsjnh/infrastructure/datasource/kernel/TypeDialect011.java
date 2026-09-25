package com.klsjnh.infrastructure.datasource.kernel;

/*                TypeDialect011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  column-type dialect resolver (registry)
 *
 */

import com.klsjnh.common.constant.DatabaseTypes011;
import com.klsjnh.common.exception.BusinessException;

import com.klsjnh.domain.datasource.kernel.ColumnMeta;
import com.klsjnh.domain.datasource.kernel.TypeDialectPort011;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Column-type dialect resolver: looks up an open registry of
 * {@link TypeDialectPort011} beans by the target database type. A new database
 * is supported by adding a dialect bean; the platform is not modified.
 */

@Component
public class TypeDialect011 {

    /**
     * Normalized db type to dialect index.
     */
    private final Map<String, TypeDialectPort011> byType = new HashMap<>();

    /**
     * Create the resolver from all type dialect beans on the classpath.
     *
     * @param dialects type dialects
     */
    public TypeDialect011(List<TypeDialectPort011> dialects) {
        for (TypeDialectPort011 dialect : dialects) {
            byType.put(DatabaseTypes011.normalize(dialect.dbType()), dialect);
        }
    }

    /**
     * Resolve the type dialect for a database type.
     *
     * @param dsType database type (mysql / postgresql / oracle / sqlserver)
     * @return dialect
     */
    public TypeDialectPort011 of(String dsType) {
        String type = DatabaseTypes011.normalize(dsType);
        TypeDialectPort011 dialect = type == null ? null : byType.get(type);

        if (dialect == null) {
            throw BusinessException.badRequest("unknown database type: " + dsType);
        }

        return dialect;
    }

    /**
     * Render a target column type for a database type.
     *
     * @param dsType database type
     * @param column column metadata
     * @return sql type clause
     */
    public String columnType(String dsType, ColumnMeta column) {
        return of(dsType).columnType(column);
    }

    /**
     * Map a JDBC type to a neutral field type for a database type.
     *
     * @param dsType   database type
     * @param jdbcType {@link java.sql.Types} constant
     * @return neutral field type code
     */
    public String jdbcToNeutral(String dsType, int jdbcType) {
        return of(dsType).jdbcToNeutral(jdbcType);
    }
}
