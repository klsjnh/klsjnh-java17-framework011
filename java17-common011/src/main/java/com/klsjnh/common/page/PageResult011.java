package com.klsjnh.common.page;

/*                PageResult011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  page result 011 class
 *
 */

import java.util.List;

/**
 * Pagination envelope payload: pageIndex/pageSize/total/totalPages/rows.
 *
 * @param pageIndex  current page, starts at 1
 * @param pageSize   page size
 * @param total      total row count
 * @param totalPages total page count
 * @param rows       current page rows, never null
 */

public record PageResult011<T>(int pageIndex, int pageSize, long total, long totalPages, List<T> rows) {

    /**
     * Build a page result from a normalized query.
     *
     * @param query normalized page query
     * @param total total row count
     * @param rows  current page rows
     * @param <T>   row type
     * @return page result with rows never null
     */
    public static <T> PageResult011<T> of(PageQuery011 query, long total, List<T> rows) {
        List<T> safeRows = rows == null ? List.of() : List.copyOf(rows);
        long totalPages = (total + query.pageSize() - 1) / query.pageSize();

        return new PageResult011<>(query.pageIndex(), query.pageSize(), total, totalPages, safeRows);
    }
}
