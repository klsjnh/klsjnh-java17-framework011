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
 *
 */

import com.klsjnh.infrastructure.persistence.entity.TreePo;
import com.klsjnh.infrastructure.persistence.mapper.CommonMapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tree repository base: {@link BaseRepository} plus {@link #selectTree()},
 * which loads all alive rows and assembles them into a nested tree rooted at a
 * blank {@code parent_id}.
 * <p>
 * Orphan rows (parent missing or soft-deleted) and cycle-corrupted rows fall
 * back to the root level so they are never silently lost.
 * </p>
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
        return buildTree(selectList(treeWrapper()));
    }

    /**
     * Build the wrapper used by {@link #selectTree()}.
     * <p>
     * Default orders by {@code id}; subclasses override for other ordering (the
     * 011 tier orders by {@code sort_order} / {@code id}).
     * </p>
     *
     * @return ordered query wrapper
     */
    protected QueryWrapper<T> treeWrapper() {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("id");

        return wrapper;
    }

    /**
     * Assemble flat ordered rows into a parent-child tree. Roots are rows with
     * a blank parent_id; orphan rows (parent missing or soft-deleted) and
     * cycle-corrupted rows fall back to the root level.
     *
     * @param rows ordered alive rows
     * @return root nodes with nested children
     */
    private List<T> buildTree(List<T> rows) {
        Map<String, T> byId = new HashMap<>();

        for (T row : rows) {
            byId.put(row.getId(), row);
        }

        List<T> roots = new ArrayList<>();

        for (T row : rows) {
            String parent = row.getParentId();

            if (parent == null || parent.isBlank()) {
                roots.add(row);
                continue;
            }

            T parentNode = byId.get(parent);

            if (parentNode == null) {
                roots.add(row);
                continue;
            }

            parentNode.getChildren().add(row);
        }

        Set<String> reachable = new HashSet<>();

        for (T root : roots) {
            collectIds(root, reachable);
        }

        for (T row : rows) {
            if (!reachable.contains(row.getId())) {
                roots.add(row);
                collectIds(row, reachable);
            }
        }

        return roots;
    }

    /**
     * Collect node ids reachable from a node, cutting at already visited ids.
     *
     * @param node tree node
     * @param ids  reachable id set
     */
    private void collectIds(T node, Set<String> ids) {
        if (!ids.add(node.getId())) {
            return;
        }

        for (T child : node.getChildren()) {
            collectIds(child, ids);
        }
    }
}
