package com.klsjnh.infrastructure.persistence.entity;

/*                BasePo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  base po 011 class
 *
 */

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Sorted persistence PO: BasePo plus sort_order for manually ordered tables.
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class BasePo011 extends BasePo {

    /** Sort order, smaller comes first, default 9999. */
    private Integer sortOrder;
}
