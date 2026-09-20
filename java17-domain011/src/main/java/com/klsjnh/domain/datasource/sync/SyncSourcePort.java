package com.klsjnh.domain.datasource.sync;

/*                SyncSourcePort interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  sync source port
 *
 */

import java.util.List;
import java.util.Map;

/**
 * Sync source port: read one page of rows from an endpoint. The implementation
 * reuses the datasource center's parameterized paged query, so pagination and
 * dialect handling are shared.
 */

public interface SyncSourcePort {

    /**
     * Read one page from a source endpoint.
     *
     * @param endpoint source endpoint
     * @param pageSize page size
     * @param pageIndex page index starting at 1
     * @return rows as column-value maps
     */
    List<Map<String, Object>> readPage(Endpoint endpoint, int pageSize, int pageIndex);
}
