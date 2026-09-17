package com.klsjnh.domain.lowcode011;

/*                MetadataDdlExecutorPort interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  metadata ddl executor port
 *
 */

import java.util.Set;

/**
 * DDL execution port: runs the generated statements against the target store,
 * and exposes just enough catalog reads to make publishing idempotent (create
 * only when missing; add only the columns that are not there yet).
 */

public interface MetadataDdlExecutorPort {

    /**
     * Whether the physical table exists.
     *
     * @param physicalTable physical table name
     * @return true when present
     */
    boolean tableExists(String physicalTable);

    /**
     * Existing column names of a physical table (lower case).
     *
     * @param physicalTable physical table name
     * @return column names, empty when the table is absent
     */
    Set<String> columnsOf(String physicalTable);

    /**
     * Execute one DDL statement.
     *
     * @param ddl ddl statement (no trailing semicolon)
     */
    void execute(String ddl);
}
