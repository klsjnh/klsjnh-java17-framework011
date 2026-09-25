package com.klsjnh.infrastructure.persistence.entity;

/*                TreePo class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  tree po class
 *      2026.09.12  add non-column children list for tree assembly
 *
 */

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.baomidou.mybatisplus.annotation.TableField;

import java.util.ArrayList;
import java.util.List;

/**
 * Tree persistence PO: BasePo plus parent_id and the non-column children list
 * used by tree assembly.
 * <p>
 * This is the <b>unsorted</b> tree shape: it declares no {@code sort_order}
 * column. Tree repositories default to ordering by {@code sort_order, id}, so a
 * table built on this PO must override {@code treeWrapper()} and order
 * explicitly — otherwise the generated SQL references a column that does not
 * exist. Prefer {@link TreePo011} (tree + sort_order) for real tables.
 * </p>
 *
 * @param <T> concrete tree PO type
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class TreePo<T extends TreePo<T>> extends BasePo {

    /** Parent node id, blank for root. */
    private String parentId;

    /** Nested child nodes, not a table column; filled by tree assembly. */
    @TableField(exist = false)
    private List<T> children = new ArrayList<>();
}
