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
 *
 */

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Tree persistence PO: BasePo plus parent_id for tree tables.
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class TreePo extends BasePo {

    /** Parent node id, blank for root. */
    private String parentId;
}
