package com.klsjnh.infrastructure.persistence.repository;

/*                BaseTreeRepository011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  base tree repository 011 class
 *
 */

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.klsjnh.infrastructure.persistence.entity.TreePo011;
import com.klsjnh.infrastructure.persistence.mapper.CommonMapper;

/**
 * Sorted tree repository base: {@link BaseTreeRepository} with tree ordering by
 * {@code sort_order} / {@code id}. The 011 tier of the tree chain.
 *
 * @param <T> sorted tree PO type (extends {@link TreePo011})
 * @param <M> mapper type
 */

public abstract class BaseTreeRepository011<T extends TreePo011<T>, M extends BaseMapper<T>>
        extends BaseTreeRepository<T, M> {

    /**
     * Create the sorted tree repository base.
     *
     * @param mapper       mybatis-plus mapper
     * @param commonMapper native sql mapper
     */
    protected BaseTreeRepository011(M mapper, CommonMapper commonMapper) {
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
