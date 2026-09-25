package com.klsjnh.infrastructure.persistence.support;

/*                TreeAssembler class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.21
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.21  tree assembler (extracted from BaseTreeRepository)
 *
 */

import com.klsjnh.infrastructure.persistence.entity.TreePo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tree assembly helper: turns flat ordered rows into a nested tree rooted at a
 * blank {@code parent_id}. Shared by the tree repository bases so the assembly
 * rules live in one place.
 * <p>
 * Orphan rows (parent missing or soft-deleted) and cycle-corrupted rows fall
 * back to the root level so they are never silently lost.
 * </p>
 */

public final class TreeAssembler {

    /**
     * Not instantiable.
     */
    private TreeAssembler() {
    }

    /**
     * Assemble flat ordered rows into a parent-child tree. Roots are rows with
     * a blank parent_id; orphan rows (parent missing or soft-deleted) and
     * cycle-corrupted rows fall back to the root level.
     *
     * @param rows ordered alive rows
     * @param <T>  tree PO type
     * @return root nodes with nested children
     */
    public static <T extends TreePo<T>> List<T> build(List<T> rows) {
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
     * @param <T>  tree PO type
     */
    private static <T extends TreePo<T>> void collectIds(T node, Set<String> ids) {
        if (!ids.add(node.getId())) {
            return;
        }

        for (T child : node.getChildren()) {
            collectIds(child, ids);
        }
    }
}
