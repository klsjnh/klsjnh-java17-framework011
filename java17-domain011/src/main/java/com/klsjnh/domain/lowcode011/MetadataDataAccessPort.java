package com.klsjnh.domain.lowcode011;

/*                MetadataDataAccessPort interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  metadata data access port
 *
 */

import java.util.List;
import java.util.Map;

/**
 * Generic data access for generated physical tables, used by the runtime and
 * the open API. Table/column names are validated by the implementation; all
 * values are bound.
 */

public interface MetadataDataAccessPort {

    /**
     * Page rows with equality filters.
     *
     * @param table   physical table
     * @param columns selected columns
     * @param filters equality filters (column → value)
     * @param orderBy order column, nullable
     * @param offset  zero based offset
     * @param limit   page size
     * @return rows
     */
    List<Map<String, Object>> select(String table, List<String> columns, Map<String, Object> filters, String orderBy,
            int offset, int limit);

    /**
     * Count rows with equality filters.
     *
     * @param table   physical table
     * @param filters equality filters
     * @return row count
     */
    long count(String table, Map<String, Object> filters);

    /**
     * Insert one row.
     *
     * @param table  physical table
     * @param values column → value
     * @return affected rows
     */
    int insert(String table, Map<String, Object> values);

    /**
     * Update one row by key.
     *
     * @param table     physical table
     * @param keyColumn key column
     * @param keyValue  key value
     * @param values    column → value
     * @return affected rows
     */
    int updateByKey(String table, String keyColumn, Object keyValue, Map<String, Object> values);

    /**
     * Delete one row by key (logic delete when the table has a {@code dr}).
     *
     * @param table     physical table
     * @param keyColumn key column
     * @param keyValue  key value
     * @param logic     whether to logic delete
     * @return affected rows
     */
    int deleteByKey(String table, String keyColumn, Object keyValue, boolean logic);
}
