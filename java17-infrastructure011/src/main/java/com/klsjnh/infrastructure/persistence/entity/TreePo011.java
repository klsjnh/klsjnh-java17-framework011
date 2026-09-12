package com.klsjnh.infrastructure.persistence.entity;

/*                TreePo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  tree po 011 class
 *
 */

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Tree persistence PO with sort order: TreePo plus sort_order
 * for sibling-ordered tree tables (menus, departments).
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class TreePo011 extends TreePo {

    /** Sort order, smaller comes first, default 9999. */
    private Integer sortOrder;
}
