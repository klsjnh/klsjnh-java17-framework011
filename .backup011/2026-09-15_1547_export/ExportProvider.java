package com.klsjnh.application.export;

/*                ExportProvider interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  export provider interface
 *
 */

import java.util.List;
import java.util.Map;

/**
 * Export provider contract: one registered provider per exportable object.
 * Rows are column-keyed ordered maps — the platform serializes them to the
 * requested format. Providers MUST NOT include sensitive columns (e.g.
 * password hashes).
 */

public interface ExportProvider {

    /**
     * Get the unique object code this provider exports (e.g. julyUser).
     *
     * @return object code
     */
    String objectCode();

    /**
     * Export all alive rows as ordered column-keyed maps (capped by the
     * platform at 500 rows).
     *
     * @return rows
     */
    List<Map<String, Object>> exportRows();
}
