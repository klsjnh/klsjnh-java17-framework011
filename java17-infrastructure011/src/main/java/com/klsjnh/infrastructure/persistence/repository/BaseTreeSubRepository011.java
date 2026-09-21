package com.klsjnh.infrastructure.persistence.repository;

/*                BaseTreeSubRepository011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.21
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.21  sorted tree master + child repository base
 *
 */

import com.klsjnh.infrastructure.persistence.entity.TreePo011;
import com.klsjnh.infrastructure.persistence.mapper.CommonMapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * Sorted tree master-sub repository base: {@link BaseTreeSubRepository} with
 * tree ordering by {@code sort_order} / {@code id}. The 011 tier of the
 * tree-sub chain.
 *
 * @param <T> sorted tree master PO type (extends {@link TreePo011})
 * @param <M> master mapper type
 */

public abstract class BaseTreeSubRepository011<T extends TreePo011<T>, M extends BaseMapper<T>>
        extends BaseTreeSubRepository<T, M> {

    /**
     * Create the sorted tree master-sub repository base.
     *
     * @param mapper       master mapper
     * @param commonMapper native sql mapper
     */
    protected BaseTreeSubRepository011(M mapper, CommonMapper commonMapper) {
        super(mapper, commonMapper);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The 011 tier orders by {@code sort_order} first, then {@code id}.</p>
     */
    @Override
    protected QueryWrapper<T> treeWrapper() {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("sort_order").orderByAsc("id");

        return wrapper;
    }
}
