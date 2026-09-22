package com.klsjnh.infrastructure.persistence.support;

/*                SortSupportTest class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.22  unit test for SortSupport default order (031/017)
 *
 */

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link SortSupport}: default business order is
 * {@code sort_order} ASC then {@code id} ASC.
 */

class SortSupportTest {

    /**
     * {@link SortSupport#orderBySortThenId} appends both columns to an existing
     * wrapper.
     */
    @Test
    void orderBySortThenIdAppendsSortOrderThenId() {
        QueryWrapper<Object> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "1");

        SortSupport.orderBySortThenId(wrapper);

        String sql = wrapper.getCustomSqlSegment().toLowerCase();

        Assertions.assertTrue(sql.contains("sort_order"), sql);
        Assertions.assertTrue(sql.contains("id"), sql);
        Assertions.assertTrue(sql.indexOf("sort_order") < sql.lastIndexOf("id"), sql);
    }

    /**
     * {@link SortSupport#sortedWrapper} returns a fresh ordered wrapper.
     */
    @Test
    void sortedWrapperIsAlreadyOrdered() {
        String sql = SortSupport.sortedWrapper().getCustomSqlSegment().toLowerCase();

        Assertions.assertTrue(sql.contains("sort_order"), sql);
        Assertions.assertTrue(sql.contains("id"), sql);
    }
}
