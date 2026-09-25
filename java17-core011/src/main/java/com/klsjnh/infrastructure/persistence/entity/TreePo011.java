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
 *      2026.09.12  children list moved up to TreePo
 *
 */

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Sorted tree persistence PO: TreePo plus sort_order. The 011 tier of the
 * tree PO family.
 *
 * @param <T> concrete tree PO type
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class TreePo011<T extends TreePo011<T>> extends TreePo<T> {

    /** Sort order, smaller comes first, default 9999. */
    private Integer sortOrder;
}
