package com.klsjnh.common.page;

/*                PageQuery011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  page query 011 class
 *
 */

/**
 * Shared pagination query record. pageIndex starts at 1; pageSize falls back
 * to 10.
 *
 * @param pageIndex requested page, starts at 1
 * @param pageSize  requested page size
 */

public record PageQuery011(Integer pageIndex, Integer pageSize) {

    /**
     * Create the query with normalization.
     *
     * @param pageIndex requested page, starts at 1
     * @param pageSize  requested page size
     */
    public PageQuery011 {
        if (pageIndex == null || pageIndex < 1) {
            pageIndex = 1;
        }
        if (pageSize == null || pageSize <= 0) {
            pageSize = 10;
        }
    }

    /**
     * Calculate the row offset for the current page.
     *
     * @return zero-based offset
     */
    public int offset() {
        return (pageIndex - 1) * pageSize;
    }
}
