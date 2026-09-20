package com.klsjnh.domain.datasource.sync;

/*                SyncSinkPort interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  sync sink port
 *
 */

import java.util.List;
import java.util.Map;

/**
 * Sync sink port: write one page of mapped rows to a target endpoint, keyed for
 * idempotent upsert (or plain append).
 */

public interface SyncSinkPort {

    /**
     * Write one page to a target endpoint.
     *
     * @param endpoint    target endpoint
     * @param targetTable target table name
     * @param keyColumns  business key columns (empty for append)
     * @param upsert      true = upsert by key, false = append
     * @param rows        mapped rows (target column -> value)
     * @return number of rows written
     */
    int writePage(Endpoint endpoint, String targetTable, List<String> keyColumns, boolean upsert,
            List<Map<String, Object>> rows);
}
