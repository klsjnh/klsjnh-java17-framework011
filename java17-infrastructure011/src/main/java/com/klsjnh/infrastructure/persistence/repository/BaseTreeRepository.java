package com.klsjnh.infrastructure.persistence.repository;

/*                BaseTreeRepository class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  base tree repository class
 *      2026.09.21  tree assembly extracted to TreeAssembler (shared with tree-sub base)
 *
 */

import com.klsjnh.infrastructure.persistence.entity.TreePo;
import com.klsjnh.infrastructure.persistence.mapper.CommonMapper;
import com.klsjnh.infrastructure.persistence.support.SortSupport;
import com.klsjnh.infrastructure.persistence.support.TreeAssembler;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * Tree repository base: {@link BaseRepository} plus {@link #selectTree()},
 * which loads all alive rows and assembles them into a nested tree rooted at a
 * blank {@code parent_id} (assembly rules live in {@link TreeAssembler}).
 *
 * @param <T> tree PO type (extends {@link TreePo})
 * @param <M> mapper type
 */

public abstract class BaseTreeRepository<T extends TreePo<T>, M extends BaseMapper<T>>
        extends BaseRepository<T, M> {

    /**
     * Create the tree repository base.
     *
     * @param mapper       mybatis-plus mapper
     * @param commonMapper native sql mapper
     */
    protected BaseTreeRepository(M mapper, CommonMapper commonMapper) {
        super(mapper, commonMapper);
    }

    /**
     * Load alive rows and assemble the nested tree rooted at a blank parent_id.
     *
     * @return root nodes with nested children
     */
    public List<T> selectTree() {
        return TreeAssembler.build(selectList(treeWrapper()));
    }

    /**
     * Build the wrapper used by {@link #selectTree()}.
     * <p>
     * Defaults to the business order {@code sort_order ASC, id ASC} via
     * {@link SortSupport}. A tree table <b>without</b> a {@code sort_order}
     * column must override this method and order explicitly (e.g. by
     * {@code id}) — the default would fail on the missing column.
     * </p>
     *
     * @return ordered query wrapper
     */
    protected QueryWrapper<T> treeWrapper() {
        return SortSupport.sortedWrapper();
    }
}
