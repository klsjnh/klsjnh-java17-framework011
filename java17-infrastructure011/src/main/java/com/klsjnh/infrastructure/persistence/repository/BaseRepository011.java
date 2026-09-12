package com.klsjnh.infrastructure.persistence.repository;

/*                BaseRepository011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  base repository 011 class
 *
 */

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.klsjnh.infrastructure.persistence.entity.BasePo011;
import com.klsjnh.infrastructure.persistence.mapper.CommonMapper;

/**
 * Sorted repository base: {@link BaseRepository} plus sort_order aware helpers
 * for tables with manual ordering. The 011 tier of the repository base family.
 *
 * @param <T> sorted PO type (extends {@link BasePo011})
 * @param <M> mapper type
 */

public abstract class BaseRepository011<T extends BasePo011, M extends BaseMapper<T>>
        extends BaseRepository<T, M> {

    /**
     * Create the sorted repository base.
     *
     * @param mapper       mybatis-plus mapper
     * @param commonMapper native sql mapper
     */
    protected BaseRepository011(M mapper, CommonMapper commonMapper) {
        super(mapper, commonMapper);
    }

    /**
     * Standard ordering wrapper: order by sort_order, then id.
     *
     * @return ordered query wrapper
     */
    protected QueryWrapper<T> sortedWrapper() {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("sort_order").orderByAsc("id");

        return wrapper;
    }
}
