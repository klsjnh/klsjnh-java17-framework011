package com.klsjnh.infrastructure.persistence.repository;

/*                BaseTreeSubRepository class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.21
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.21  tree master + child repository base
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
 * Tree master-sub repository base: a tree master ({@code parent_id}) that also
 * owns child tables ({@code pk_mt}) — {@link BaseMasterSubRepository} plus
 * {@link #selectTree()}. The tree assembly rules live in {@link TreeAssembler};
 * the cascade rules in {@code MasterSubSupport}.
 * <p>
 * The tree chain and the master-sub chain cannot be combined by inheritance
 * (Java single inheritance), so this base extends the master-sub chain and
 * re-adds the tree read side; {@link BaseTreeRepository} stays the base for
 * childless trees.
 * </p>
 *
 * @param <T> tree master PO type (extends {@link TreePo})
 * @param <M> master mapper type
 */

public abstract class BaseTreeSubRepository<T extends TreePo<T>, M extends BaseMapper<T>>
        extends BaseMasterSubRepository<T, M> {

    /**
     * Create the tree master-sub repository base.
     *
     * @param mapper       master mapper
     * @param commonMapper native sql mapper
     */
    protected BaseTreeSubRepository(M mapper, CommonMapper commonMapper) {
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
