package com.klsjnh.application.lowcode011;

/*                ObjectQueryCommand record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  object query command record
 *
 */

import java.util.Map;

/**
 * Typed query command for a generated object's row page: the object name, the
 * equality filters and the page request. The web layer builds it from the
 * request body so the application boundary no longer takes a bare Map.
 *
 * @param objectName object name
 * @param filters    equality filters, never null
 * @param pageIndex  page index, nullable (default 1)
 * @param pageSize   page size, nullable (default 20, clamped [1,500])
 */

public record ObjectQueryCommand(String objectName, Map<String, Object> filters, Integer pageIndex,
        Integer pageSize) {

    /**
     * Normalize the filter map.
     */
    public ObjectQueryCommand {
        filters = filters == null ? Map.of() : Map.copyOf(filters);
    }
}
