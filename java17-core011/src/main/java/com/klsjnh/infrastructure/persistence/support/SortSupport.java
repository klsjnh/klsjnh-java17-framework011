package com.klsjnh.infrastructure.persistence.support;

/*                SortSupport class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.22  sort helper (sort_order, id) — replaces the *011 repo sort tier
 *
 */

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 * Sort helper: the default business ordering convention — {@code sort_order}
 * ascending, then {@code id} ascending.
 * <p>
 * Sorting is a column convention plus a query habit, not a repository
 * lifecycle, so it lives here as a static helper (same pattern as
 * {@link TreeAssembler} and {@code MasterSubSupport}) instead of an
 * inheritance tier. Base classes call {@link #sortedWrapper()}; business
 * repositories apply {@link #orderBySortThenId(QueryWrapper)} to the wrapper
 * they already build — no repository may introduce a sort-order base class
 * again.
 * </p>
 */

public final class SortSupport {

    /**
     * Business sort column, smaller comes first.
     */
    public static final String SORT_COLUMN = "sort_order";

    /**
     * Tie-breaker column.
     */
    public static final String ID_COLUMN = "id";

    /**
     * Not instantiable.
     */
    private SortSupport() {
    }

    /**
     * Apply the default business sort to an existing wrapper: {@code sort_order}
     * ASC, then {@code id} ASC.
     *
     * @param wrapper query wrapper
     * @param <T>     PO type
     * @return the same wrapper, ordered
     */
    public static <T> QueryWrapper<T> orderBySortThenId(QueryWrapper<T> wrapper) {
        return wrapper.orderByAsc(SORT_COLUMN).orderByAsc(ID_COLUMN);
    }

    /**
     * New empty wrapper already ordered by {@code sort_order}, {@code id}.
     * <p>
     * Base-class internal use (tree assembly) — business repositories must
     * apply {@link #orderBySortThenId(QueryWrapper)} to their own wrapper
     * instead of building a wrapper through this method.
     * </p>
     *
     * @param <T> PO type
     * @return ordered query wrapper
     */
    public static <T> QueryWrapper<T> sortedWrapper() {
        return orderBySortThenId(new QueryWrapper<>());
    }
}
